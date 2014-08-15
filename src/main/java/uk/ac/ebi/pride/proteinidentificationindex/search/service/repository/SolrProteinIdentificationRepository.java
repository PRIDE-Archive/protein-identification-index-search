package uk.ac.ebi.pride.proteinidentificationindex.search.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;

import java.util.Collection;
import java.util.List;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 * Note: using the Query annotation allows wildcards to go straight into the query
 */
public interface SolrProteinIdentificationRepository extends SolrCrudRepository<ProteinIdentification, String> {


    // Id query methods
    @Query("id:?0")
    List<ProteinIdentification> findById(String id);
    @Query("id:(?0)")
    List<ProteinIdentification> findByIdIn(Collection<String> id);

    // Accession query methods
    @Query("accession:?0")
    List<ProteinIdentification> findByAccession(String accession);
    @Query("accession:(?0)")
    List<ProteinIdentification> findByAccessionIn(Collection<String> accession);

    // Project accession query methods
    @Query("project_accession:?0")
    List<ProteinIdentification> findByProjectAccession(String projectAccession);
    @Query("project_accession:(?0)")
    List<ProteinIdentification> findByProjectAccessionIn(Collection<String> projectAccessions);
    @Query("project_accession:?0")
    Page<ProteinIdentification> findByProjectAccession(String projectAccession, Pageable pageable);
    @Query("project_accession:(?0)")
    Page<ProteinIdentification> findByProjectAccessionIn(Collection<String> projectAccessions, Pageable pageable);

    // Assay accession query methods
    @Query("assay_accession:?0")
    List<ProteinIdentification> findByAssayAccession(String assayAccession);
    @Query("assay_accession:(?0)")
    List<ProteinIdentification> findByAssayAccessionIn(Collection<String> assayAccessions);
    @Query("assay_accession:?0")
    Page<ProteinIdentification> findByAssayAccession(String assayAccession, Pageable pageable);
    @Query("assay_accession:(?0)")
    Page<ProteinIdentification> findByAssayAccessionIn(Collection<String> assayAccessions, Pageable pageable);

}
