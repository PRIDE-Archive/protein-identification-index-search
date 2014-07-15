package uk.ac.ebi.pride.proteinidentificationindex.search.service.repository;

import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.repository.support.SolrRepositoryFactory;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class SolrProteinIdentificationRepositoryFactory {

    private SolrOperations solrOperations;

    public SolrProteinIdentificationRepositoryFactory(SolrOperations solrOperations) {
        this.solrOperations = solrOperations;
    }

    public SolrProteinIdentificationRepository create() {
        return new SolrRepositoryFactory(this.solrOperations).getRepository(SolrProteinIdentificationRepository.class);
    }

}