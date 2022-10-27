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
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@SpringBootApplication
public class BrcrisApplication {

	private static final int QTD_ITEMS_BY_SEARCH = 100;
	private static int from = 0;

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



		List<PqSeniorPersResp> pqseniorpers = getAuthors(client);

		getPubs(client, pqseniorpers);


	}

	private static void getPubs(ElasticsearchClient client, List<PqSeniorPersResp> pqseniorpers)
			throws IOException {
		System.out.println("consulta pubs");
		long totalPubs = searchPubs(client, 0, 0).total().value();
		from = 0;
		while (from < totalPubs) {
			Set<Publication> publications = new HashSet<>();
			List<Hit<PqSeniorsPubsResp>> resp =
					searchPubs(client, from, QTD_ITEMS_BY_SEARCH).hits();
			if (resp == null || resp.isEmpty()) {
				break;
			}
			for (Hit<PqSeniorsPubsResp> hit : resp) {
				PqSeniorsPubsResp pq = hit.source();
				Publication pub = new Publication(pq.getId());
				for (Author author : pq.getAuthor()) {

					PqSeniorPersResp orElse = pqseniorpers.stream()
							.filter(p -> p.getId().equals(author.getId())).findFirst().orElse(null);
					if (orElse != null) {
						Institution institution =
								new Institution(orElse.getOrgunit().get(0).getId(),
										orElse.getOrgunit().get(0).getName().get(0));
						pub.addInstitution(institution);

					}

				}
				publications.add(pub);
			}

			if (publications.size() > 0) {
				System.out.println("inserindo");
				insertPub(client, publications);
				System.out.println("foiii");
			}
			from += QTD_ITEMS_BY_SEARCH;
		}
	}

	private static List<PqSeniorPersResp> getAuthors(ElasticsearchClient client) {
		List<PqSeniorPersResp> pqseniorpers = new ArrayList<>();
		System.out.println("consulta autores");
		long totalAuthors = searchAuthors(client, 0, 0).total().value();
		int from = 0;
		while (from < totalAuthors) {
			List<Hit<PqSeniorPersResp>> resp =
					searchAuthors(client, from, QTD_ITEMS_BY_SEARCH).hits();

			if (resp == null || resp.isEmpty()) {
				break;
			}
			for (Hit<PqSeniorPersResp> hit : resp) {
				pqseniorpers.add(hit.source());
			}
			from += QTD_ITEMS_BY_SEARCH;
		}
		System.out.println("pqseniorpers size: " + pqseniorpers.size());
		return pqseniorpers;
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



	private static HitsMetadata<PqSeniorsPubsResp> searchPubs(ElasticsearchClient client, int from,
			int size) {
		try {
			System.out.println("Busca " + QTD_ITEMS_BY_SEARCH + " a partir de " + from);
			SearchResponse<PqSeniorsPubsResp> search =
					client.search(
							s -> s.index("pqseniors-pubs").from(from).size(size)
									.source(so -> so.filter(fil -> fil.includes(
											Arrays.asList("author.name", "author.id", "id"))))
									.query(q -> q.constantScore(
											c -> c.filter(f -> f.exists(e -> e.field("author"))))),
							PqSeniorsPubsResp.class);

			return search.hits();
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

	private static HitsMetadata<PqSeniorPersResp> searchAuthors(ElasticsearchClient client,
			int from, int size) {
		try {
			System.out.println("Busca " + QTD_ITEMS_BY_SEARCH + " a partir de " + from);
			SearchResponse<PqSeniorPersResp> search =
					client.search(
							s -> s.index("pqsenior-pers").from(from).size(size)
									.source(so -> so.filter(fil -> fil.includes(Arrays
											.asList("orgunit.name", "name", "id", "orgunit.id"))))
									.query(q -> q.constantScore(c -> c
											.filter(f -> f.exists(e -> e.field("orgunit.name"))))),
							PqSeniorPersResp.class);

			return search.hits();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}



}
