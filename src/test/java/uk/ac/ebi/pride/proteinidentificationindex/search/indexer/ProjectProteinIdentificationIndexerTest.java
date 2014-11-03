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
import org.springframework.data.solr.core.SolrTemplate;
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

    private static final String TEST_SUBMITTED_SEQ = "MSSEEVVVAVEEQEIPDVIERLMSSEEVVVAVEEQEIPDVIERLMSSEEVVVAVEEQEIPDVIERL";
    private static final String TEST_ID = "TEST_ID";
    private static final String TEST_SUBMITTED_AC = "D0NNB3";

    private static final String TEST_PROTEIN_ACCESSION = "D0NNB3";
    private static final String TEST_PROTEIN_NAME_FIELD = "NAME####Putative uncharacterized protein";
    private static final String TEST_PROTEIN_SEQ_STARTS_WITH = "MSSEEVVVAVEEQEIPDVIERL";
    private static SolrServer server;

    private static SolrProteinIdentificationRepositoryFactory solrProteinIdentificationRepositoryFactory;

    public static final long ZERO_DOCS = 0L;

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
        assertEquals(proteins.get(0).getSubmittedAccession(), TEST_SUBMITTED_AC);
        assertTrue(proteins.get(0).getSubmittedSequence().equals(TEST_SUBMITTED_SEQ));

    }


    private void addD0NNb3() {
        ProteinIdentificationIndexService proteinIdentificationIndexService = new ProteinIdentificationIndexService(server, solrProteinIdentificationRepositoryFactory.create());
        ProteinIdentification proteinIdentification = new ProteinIdentification();
        proteinIdentification.setId(TEST_ID);
        proteinIdentification.setAccession(TEST_PROTEIN_ACCESSION);
        proteinIdentification.setSubmittedAccession(TEST_SUBMITTED_AC);
        proteinIdentification.setSubmittedSequence(TEST_SUBMITTED_SEQ);
        proteinIdentificationIndexService.save(proteinIdentification);
    }


}
