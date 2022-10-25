package br.ibict.brcris;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import br.ibict.brcris.dto.Institution;
import br.ibict.brcris.dto.Publication;
import br.ibict.brcris.response.Author;
import br.ibict.brcris.response.PqSeniorPersResp;
import br.ibict.brcris.response.PqSeniorsPubsResp;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@SpringBootApplication
public class BrcrisApplication {

	private static final int QTD = 100;

	public static void main(String[] args) {
		SpringApplication.run(BrcrisApplication.class, args);
		try {
			init();
		} catch (ElasticsearchException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void init() throws ElasticsearchException, IOException {

		RestClient restClient = RestClient.builder(new HttpHost("172.16.16.90", 9200)).build();
		ElasticsearchTransport transport =
				new RestClientTransport(restClient, new JacksonJsonpMapper());
		ElasticsearchClient client = new ElasticsearchClient(transport);


		// List<Hit<PqSeniorPersResp>> authorsById = getAuthorsById(client,
		// Arrays.asList("34fcd70b-4c06-417e-a207-374ad5d11a5a",
		// "8a0de9da-6f1d-4fac-99da-0b4133d2b07a",
		// "ff0c9859-a1c5-4df4-9737-68d871f08cf1"));

		// System.out.println(authorsById.size());

		// for (Hit<PqSeniorPersResp> hit : authorsById) {

		// PqSeniorPersResp pq = hit.source();
		// System.out.println(pq.getName().get(0));

		// }


		List<PqSeniorPersResp> pqseniorpers = new ArrayList<>();

		System.out.println("consulta autores");
		for (int contador = 0; contador < 14353; contador += QTD) {

			List<Hit<PqSeniorPersResp>> resp = getAuthors(client, contador);
			if (resp != null) {
				for (Hit<PqSeniorPersResp> hit : resp) {
					pqseniorpers.add(hit.source());
				}
			} else {
				break;
			}
		}

		System.out.println("consulta pubs");
		for (int contador = 0; contador < 40773; contador += QTD) {

			Set<Publication> publications = new HashSet<>();

			List<Hit<PqSeniorsPubsResp>> resp = getPubs(client, contador);
			if (resp != null) {
				for (Hit<PqSeniorsPubsResp> hit : resp) {
					PqSeniorsPubsResp pq = hit.source();
					Publication pub = new Publication(pq.getId());
					for (Author author : pq.getAuthor()) {

						PqSeniorPersResp orElse =
								pqseniorpers.stream().filter(p -> p.getId().equals(author.getId()))
										.findFirst().orElse(null);
						if (orElse != null) {
							Institution institution =
									new Institution(orElse.getOrgunit().get(0).getId(),
											orElse.getOrgunit().get(0).getName().get(0));
							pub.addInstitution(institution);

						}

					}
					publications.add(pub);
				}

			} else {
				break;
			}
			if (publications.size() > 0) {
				System.out.println("inserindo");
				insertPub(client, publications);
				System.out.println("foiii");
			}

		}


	}

	private static void insertPub(ElasticsearchClient client, Set<Publication> pubs)
			throws IOException {
		BulkRequest.Builder br = new BulkRequest.Builder();

		for (Publication pub : pubs) {
			br.operations(op -> op
					.index(idx -> idx.index("pubs-test-jesiel").id(pub.getId()).document(pub)));
		}

		BulkResponse result = client.bulk(br.build());

		// Log errors, if any
		if (result.errors()) {
			System.out.println("Bulk had errors");
			for (BulkResponseItem item : result.items()) {
				if (item.error() != null) {
					System.out.println(item.error().reason());
				}
			}
		}
	}



	private static List<Hit<PqSeniorsPubsResp>> getPubs(ElasticsearchClient client, int contador) {
		try {
			// System.out.println("Busca de " + contador + "até " + (contador + QTD));
			SearchResponse<PqSeniorsPubsResp> search =
					client.search(
							s -> s.index("pqseniors-pubs").from(contador).size(contador + QTD)
									.source(so -> so.filter(fil -> fil.includes(
											Arrays.asList("author.name", "author.id", "id"))))
									.query(q -> q.constantScore(
											c -> c.filter(f -> f.exists(e -> e.field("author"))))),
							PqSeniorsPubsResp.class);

			return search.hits().hits();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static List<Hit<PqSeniorPersResp>> getAuthorsById(ElasticsearchClient client,
			List<String> ids) {
		try {
			List<FieldValue> fieldValues = new ArrayList<>();
			for (String id : ids) {
				fieldValues.add(FieldValue.of(id));
			}
			SearchResponse<PqSeniorPersResp> search =
					client.search(
							s -> s.index("pqsenior-pers")
									.source(so -> so.filter(fil -> fil.includes(Arrays
											.asList("orgunit.name", "name", "id", "orgunit.id"))))
									.query(q -> q.terms(t -> t.field("id.keyword")
											.terms(termos -> termos.value(fieldValues)))),
							PqSeniorPersResp.class);

			return search.hits().hits();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static List<Hit<PqSeniorPersResp>> getAuthors(ElasticsearchClient client,
			int contador) {
		try {
			// System.out.println("Busca de " + contador + "até " + (contador + QTD));
			SearchResponse<PqSeniorPersResp> search =
					client.search(
							s -> s.index("pqsenior-pers").from(contador).size(contador + QTD)
									.source(so -> so.filter(fil -> fil.includes(Arrays
											.asList("orgunit.name", "name", "id", "orgunit.id"))))
									.query(q -> q.constantScore(c -> c
											.filter(f -> f.exists(e -> e.field("orgunit.name"))))),
							PqSeniorPersResp.class);

			return search.hits().hits();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}



}
