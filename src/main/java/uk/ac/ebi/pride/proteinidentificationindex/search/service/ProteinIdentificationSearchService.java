package uk.ac.ebi.pride.proteinidentificationindex.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.repository.SolrProteinIdentificationRepository;

import java.util.Collection;
import java.util.List;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *          <p/>
 *          NOTE: protein accessions can contain chars that produce problems in solr queries ([,],:). They are replaced by _ when
 *          using the repository methods
 */
@Service
public class ProteinIdentificationSearchService {

    private SolrProteinIdentificationRepository solrProteinIdentificationRepository;

    public ProteinIdentificationSearchService(SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
        this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
    }

    public void setSolrProteinIdentificationRepository(SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
        this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
    }

    // find by id methods
    public List<ProteinIdentification> findById(String id) {
        return solrProteinIdentificationRepository.findById(id);
    }

    public List<ProteinIdentification> findById(Collection<String> ids) {
        return solrProteinIdentificationRepository.findByIdIn(ids);
    }

    // find by accession methods
    public List<ProteinIdentification> findByAccession(String accession) {
        return solrProteinIdentificationRepository.findByAccession(accession);
    }

    public List<ProteinIdentification> findByAccession(Collection<String> accessions) {
        return solrProteinIdentificationRepository.findByAccessionIn(accessions);
    }

    // Project accession query methods
    public List<ProteinIdentification> findByProjectAccession(String projectAccession) {
        return solrProteinIdentificationRepository.findByProjectAccession(projectAccession);
    }

    public List<ProteinIdentification> findByProjectAccession(Collection<String> projectAccessions) {
        return solrProteinIdentificationRepository.findByProjectAccessionIn(projectAccessions);
    }

    public Page<ProteinIdentification> findByProjectAccession(String projectAccession, Pageable pageable) {
        return solrProteinIdentificationRepository.findByProjectAccession(projectAccession, pageable);
    }

    public Page<ProteinIdentification> findByProjectAccession(Collection<String> projectAccessions, Pageable pageable) {
        return solrProteinIdentificationRepository.findByProjectAccessionIn(projectAccessions, pageable);
    }

    // Assay accession query methods
    public List<ProteinIdentification> findByAssayAccession(String assayAccession) {
        return solrProteinIdentificationRepository.findByAssayAccession(assayAccession);
    }

    public List<ProteinIdentification> findByAssayAccession(Collection<String> assayAccessions) {
        return solrProteinIdentificationRepository.findByAssayAccessionIn(assayAccessions);
    }

    public Page<ProteinIdentification> findByAssayAccession(String assayAccession, Pageable pageable) {
        return solrProteinIdentificationRepository.findByAssayAccession(assayAccession, pageable);
    }

    public Page<ProteinIdentification> findByAssayAccession(Collection<String> assayAccessions, Pageable pageable) {
        return solrProteinIdentificationRepository.findByAssayAccessionIn(assayAccessions, pageable);
    }

}
