package uk.ac.ebi.pride.proteinidentificationindex.search.indexer;


import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.SolrTemplate;
import uk.ac.ebi.pride.indexutils.results.PageWrapper;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.repository.SolrProteinIdentificationRepositoryFactory;

import java.util.List;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */

public class ProjectProteinIdentificationIndexerTest extends SolrTestCaseJ4 {


  private static Logger logger = LoggerFactory.getLogger(ProjectProteinIdentificationIndexerTest.class.getName());

  private static final String TEST_PROJ_ACCESSION = "PXT000001";
  private static final String TEST_ASSAY_ACCESSION = "123456";
  private static final String TEST_ID = "TEST_ID";

  private static final String TEST_PROTEIN_ACCESSION = "D0NNB3";
  private static SolrServer server;

  private static final String HIGHLIGHT_PRE_FRAGMENT = "<span class='search-hit'>";
  private static final String HIGHLIGHT_POST_FRAGMENT = "</span>";

  private static SolrProteinIdentificationRepositoryFactory solrProteinIdentificationRepositoryFactory;

  private static final long ZERO_DOCS = 0L;

  @BeforeClass
  public static void initialise() throws Exception {
    initCore("src/test/resources/solr/protein-identification-index/conf/solrconfig.xml",
        "src/test/resources/solr/protein-identification-index/conf/schema.xml",
        "src/test/resources/solr",
        "protein-identification-index");
  }

  @AfterClass
  public static void cleanUp() throws Exception {
    deleteCore();
  }


  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
    server = new EmbeddedSolrServer(h.getCoreContainer(), h.getCore().getName());
    server.deleteByQuery("*:*");
    solrProteinIdentificationRepositoryFactory = new SolrProteinIdentificationRepositoryFactory(new SolrTemplate(server));
  }

  @Test
  public void testThatNoResultsAreReturned() throws SolrServerException {
    SolrParams params = new SolrQuery("text that is not found");
    QueryResponse response = server.query(params);
    assertEquals(ZERO_DOCS, response.getResults().getNumFound());
  }

  @Test
  public void testAddProtein() {
    addD0NNb3();
    ProteinIdentificationSearchService proteinIdentificationSearchService = new ProteinIdentificationSearchService(solrProteinIdentificationRepositoryFactory.create());
    ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(server, solrProteinIdentificationRepositoryFactory.create());
    List<ProteinIdentification> proteins = proteinIdentificationSearchService.findByAccession(TEST_PROTEIN_ACCESSION);
    proteinIdentificationIndexService.save(proteins.get(0));
    proteins = proteinIdentificationSearchService.findByAccession(TEST_PROTEIN_ACCESSION);
    assertNotNull(proteins);
    assertNotNull(proteins.get(0));
    assertEquals(proteins.get(0).getId(), TEST_ID);
    assertEquals(proteins.get(0).getAccession(), TEST_PROTEIN_ACCESSION);
    Long count = proteinIdentificationSearchService.countByAssayAccession(TEST_ASSAY_ACCESSION);
    assertEquals(count.longValue(), 1L);
    count = proteinIdentificationSearchService.countByProjectAccession(TEST_PROJ_ACCESSION);
    assertEquals(count.longValue(), 1L);
    count = proteinIdentificationSearchService.countByProjectAccessionAndAccession(TEST_PROJ_ACCESSION, TEST_PROTEIN_ACCESSION);
    assertEquals(count.longValue(), 1L);

    PageWrapper<ProteinIdentification> highlightPage =
        proteinIdentificationSearchService.findByProjectAccessionHighlightsOnModificationNames(TEST_PROJ_ACCESSION,
            null, null, new PageRequest(0,10));
    assertNotNull(highlightPage);
    assertEquals(0, highlightPage.getHighlights().size());
    highlightPage =
        proteinIdentificationSearchService.findByProjectAccessionHighlightsOnModificationNames(TEST_PROJ_ACCESSION,
            TEST_PROTEIN_ACCESSION, null, new PageRequest(0,10));
    assertNotNull(highlightPage);
    assertEquals(TEST_PROTEIN_ACCESSION, highlightPage.getHighlights().entrySet().iterator().next().getKey().getAccession());
    assertEquals(HIGHLIGHT_PRE_FRAGMENT + TEST_PROTEIN_ACCESSION + HIGHLIGHT_POST_FRAGMENT, highlightPage.getHighlights().entrySet().iterator().next().getValue().entrySet().iterator().next().getValue().get(0));
  }


  private void addD0NNb3() {
    ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(server, solrProteinIdentificationRepositoryFactory.create());
    ProteinIdentification proteinIdentification = new ProteinIdentification();
    proteinIdentification.setId(TEST_ID);
    proteinIdentification.setProjectAccession(TEST_PROJ_ACCESSION);
    proteinIdentification.setAssayAccession(TEST_ASSAY_ACCESSION);
    proteinIdentification.setAccession(TEST_PROTEIN_ACCESSION);
    proteinIdentificationIndexService.save(proteinIdentification);
  }
}
