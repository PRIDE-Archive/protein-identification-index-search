package uk.ac.ebi.pride.proteinidentificationindex.search.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.repository.Facet;
import org.springframework.data.solr.repository.Highlight;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentificationSummary;

import java.util.Collection;
import java.util.List;

import static org.springframework.data.solr.core.query.Query.Operator.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 * Note: using the Query annotation allows wildcards to go straight into the query
 */
public interface SolrProteinIdentificationRepository extends SolrCrudRepository<ProteinIdentification, String> {

    public static final String HIGHLIGHT_PRE_FRAGMENT = "<span class='search-hit'>";
    public static final String HIGHLIGHT_POST_FRAGMENT = "</span>";

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

    // Synonym query methods
    @Query("synonyms:?0 AND project_accession:?1")
    List<ProteinIdentification> findBySynonymAndProjectAccession(String synonym, String projectAccession);

    // Project accession query methods
    Long countByProjectAccession(String projectAccession);
    @Query("project_accession:?0")
    List<ProteinIdentification> findByProjectAccession(String projectAccession);
    @Query("project_accession:(?0)")
    List<ProteinIdentification> findByProjectAccessionIn(Collection<String> projectAccessions);
    @Query("project_accession:?0")
    Page<ProteinIdentification> findByProjectAccession(String projectAccession, Pageable pageable);
    @Query("project_accession:(?0)")
    Page<ProteinIdentification> findByProjectAccessionIn(Collection<String> projectAccessions, Pageable pageable);

    @Query(value = "project_accession:?0", filters = "mod_names:(?1)", defaultOperator = AND)
    Page<ProteinIdentification> findByProjectAccessionAndFilterModNames(String projectAccessions, List<String> modNameFilters, Pageable pageable);

    @Highlight(prefix = HIGHLIGHT_PRE_FRAGMENT, postfix = HIGHLIGHT_POST_FRAGMENT, fields = {"submitted_accession, accession, ambiguity_group"})
    @Query(value = "project_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR ambiguity_group:?1)")
    HighlightPage<ProteinIdentification> findByProjectAccessionHighlights(String projectAccessions, String term, Pageable pageable);

    @Highlight(prefix = HIGHLIGHT_PRE_FRAGMENT, postfix = HIGHLIGHT_POST_FRAGMENT, fields = {"submitted_accession, accession, ambiguity_group"})
    @Query(value = "project_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR ambiguity_group:?1)", filters = "mod_names:(?2)", defaultOperator = AND)
    HighlightPage<ProteinIdentification> findByProjectAccessionHighlightsAndFilterModNames(String projectAccessions, String term, List<String> modNameFilters, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "project_accession:?0")
    FacetPage<ProteinIdentification> findByProjectAccessionFacetModNames(String projectAccessions, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "project_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR ambiguity_group:?1)")
    FacetPage<ProteinIdentification> findByProjectAccessionFacetModNames(String projectAccessions, String term, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "project_accession:?0", filters = "mod_names:(?1)", defaultOperator = AND)
    FacetPage<ProteinIdentification> findByProjectAccessionFacetModNamesAndFilterModNames(String projectAccessions, List<String> modNameFilters, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "project_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR ambiguity_group:?1)", filters = "mod_names:(?2)", defaultOperator = AND)
    FacetPage<ProteinIdentification> findByProjectAccessionFacetModNamesAndFilterModNames(String projectAccessions, String term, List<String> modNameFilters, Pageable pageable);


    // Assay accession query methods
    Long countByAssayAccession(String assayAccession);
    @Query("assay_accession:?0")
    List<ProteinIdentification> findByAssayAccession(String assayAccession);
    @Query("assay_accession:(?0)")
    List<ProteinIdentification> findByAssayAccessionIn(Collection<String> assayAccessions);
    @Query("assay_accession:?0")
    Page<ProteinIdentification> findByAssayAccession(String assayAccession, Pageable pageable);
    @Query("assay_accession:(?0)")
    Page<ProteinIdentification> findByAssayAccessionIn(Collection<String> assayAccessions, Pageable pageable);

    @Query(value = "assay_accession:?0", filters = "mod_names:(?1)", defaultOperator = AND)
    Page<ProteinIdentification> findByAssayAccessionAndFilterModNames(String assayAccession, List<String> modNameFilters, Pageable pageable);

    @Highlight(prefix = HIGHLIGHT_PRE_FRAGMENT, postfix = HIGHLIGHT_POST_FRAGMENT, fields = {"submitted_accession, accession, ambiguity_group"})
    @Query(value = "assay_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR ambiguity_group:?1)")
    HighlightPage<ProteinIdentification> findByAssayAccessionHighlights(String assayAccession, String term, Pageable pageable);

    @Highlight(prefix = HIGHLIGHT_PRE_FRAGMENT, postfix = HIGHLIGHT_POST_FRAGMENT, fields = {"submitted_accession, accession, ambiguity_group"})
    @Query(value = "assay_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR ambiguity_group:?1)", filters = "mod_names:(?2)", defaultOperator = AND)
    HighlightPage<ProteinIdentification> findByAssayAccessionHighlightsAndFilterModNames(String assayAccession, String term, List<String> modNameFilters, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "assay_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR ambiguity_group:?1)")
    FacetPage<ProteinIdentification> findByAssayAccessionFacetModNames(String assayAccession, String term, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "assay_accession:?0")
    FacetPage<ProteinIdentification> findByAssayAccessionFacetModNames(String assayAccession, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "assay_accession:?0", filters = "mod_names:(?1)", defaultOperator = AND)
    FacetPage<ProteinIdentification> findByAssayAccessionFacetModNamesAndFilterModNames(String assayAccession, List<String> modNameFilters, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "assay_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR ambiguity_group:?1)", filters = "mod_names:(?2)", defaultOperator = AND)
    FacetPage<ProteinIdentification> findByAssayAccessionFacetModNamesAndFilterModNames(String assayAccession, String term, List<String> modNameFilters, Pageable pageable);


    // Submitted accession query methods
    @Query("submitted_accession:?0 AND assay_accession:?1")
    List<ProteinIdentification> findBySubmittedAccessionAndAssayAccession(String submittedAccession, String assayAccession);

    // Queries with Projections
    @Query(value = "project_accession:?0", fields = { "accession" })
    List<ProteinIdentificationSummary> findSummaryByProjectAccession(String projectAccession);

    @Query(value = "assay_accession:?0", fields = { "accession" })
    List<ProteinIdentificationSummary> findSummaryByAssayAccession(String assayAccession);

}
