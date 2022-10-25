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
import br.ibict.brcris.model.Pqseniorpers;
import br.ibict.brcris.model.Pqseniorspubs;
import br.ibict.brcris.model.PubsAuthors;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
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
		// Create the low-level client
		RestClient restClient = RestClient.builder(new HttpHost("172.16.16.90", 9200)).build();

		// Create the transport with a Jackson mapper
		ElasticsearchTransport transport =
				new RestClientTransport(restClient, new JacksonJsonpMapper());

		// And create the API client
		ElasticsearchClient client = new ElasticsearchClient(transport);

		Set<Institution> institutions = new HashSet<>();
		Set<Pubs> pubsSave = new HashSet<>();

		List<Pqseniorpers> pqseniorpers = new ArrayList<>();

		System.out.println("consulta autores");
		for (int contador = 0; contador < 14353; contador += QTD) {

			List<Hit<Pqseniorpers>> auts = getAuthors(client, contador);
			if (auts != null) {

				for (Hit<Pqseniorpers> hit : auts) {
					pqseniorpers.add(hit.source());
					// Pqseniorpers pq = hit.source();
					// Author author = new Author(pq.getId(), pq.getName().get(0));
					// Institution institution = institutions.stream()
					// .filter(i -> i.getId().equals(pq.getOrgunit().get(0).getId()))
					// .findFirst().orElse(null);
					// if (institution != null) {
					// institution.addAuthor(author);
					// } else {
					// Institution inst = new Institution(pq.getOrgunit().get(0).getId(),
					// pq.getOrgunit().get(0).getName().get(0));
					// inst.addAuthor(author);
					// institutions.add(inst);
					// }
				}
			} else {
				break;
			}
		}

		System.out.println("consulta pubs");
		for (int contador = 0; contador < 40773; contador += QTD) {

			List<Hit<Pqseniorspubs>> pubs = getPubs(client, contador);
			if (pubs != null) {
				for (Hit<Pqseniorspubs> hit : pubs) {
					Pqseniorspubs pq = hit.source();
					Pubs pub = new Pubs(pq.getId());
					for (PubsAuthors pubsAuthors : pq.getAuthor()) {

						Pqseniorpers orElse = pqseniorpers.stream()
								.filter(p -> p.getId().equals(pubsAuthors.getId())).findFirst()
								.orElse(null);
						if (orElse != null) {
							InstitutionPub iPub =
									new InstitutionPub(orElse.getOrgunit().get(0).getId(),
											orElse.getOrgunit().get(0).getName().get(0));
							pub.addInstitution(iPub);

						}
						// for (Institution inst : institutions) {
						// Author authorFound = inst.getAuthors().stream()
						// .filter(a -> a.getId().equals(pubsAuthors.getId())).findFirst()
						// .orElse(null);
						// if (authorFound != null) {
						// authorFound.addPub(pq.getId());
						// }
						// }

					}
					pubsSave.add(pub);
				}

			} else {
				break;
			}
		}
		// for (Institution institution : institutions) {
		// System.out.println(institution.getName());
		// for (Author author : institution.getAuthors()) {
		// System.out.println(author.getName() + ": " + author.getPubs());
		// }
		// System.out.println("######");
		// }

		System.out.println("inserindo");
		insertPub(client, pubsSave);
		System.out.println("fim");



	}

	private static void insertPub(ElasticsearchClient client, Set<Pubs> pubs) throws IOException {
		BulkRequest.Builder br = new BulkRequest.Builder();

		for (Pubs pub : pubs) {
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

	private static void insert(ElasticsearchClient client, Set<Institution> institutions)
			throws IOException {
		BulkRequest.Builder br = new BulkRequest.Builder();

		for (Institution institution : institutions) {
			br.operations(op -> op.index(idx -> idx.index("institutions-test-jesiel")
					.id(institution.getId()).document(institution)));
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


	private static List<Hit<Pqseniorspubs>> getPubs(ElasticsearchClient client, int contador) {
		try {
			System.out.println("Busca de " + contador + "até " + (contador + QTD));
			SearchResponse<Pqseniorspubs> search =
					client.search(
							s -> s.index("pqseniors-pubs").from(contador).size(contador + QTD)
									.source(so -> so.filter(fil -> fil.includes(
											Arrays.asList("author.name", "author.id", "id"))))
									.query(q -> q.constantScore(
											c -> c.filter(f -> f.exists(e -> e.field("author"))))),
							Pqseniorspubs.class);

			return search.hits().hits();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}

	private static List<Hit<Pqseniorpers>> getAuthors(ElasticsearchClient client, int contador) {
		try {
			System.out.println("Busca de " + contador + "até " + (contador + QTD));
			SearchResponse<Pqseniorpers> search =
					client.search(
							s -> s.index("pqsenior-pers").from(contador).size(contador + QTD)
									.source(so -> so.filter(fil -> fil.includes(Arrays
											.asList("orgunit.name", "name", "id", "orgunit.id"))))
									.query(q -> q.constantScore(c -> c
											.filter(f -> f.exists(e -> e.field("orgunit.name"))))),
							Pqseniorpers.class);

			return search.hits().hits();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}



}
