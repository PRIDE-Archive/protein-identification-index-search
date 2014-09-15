package uk.ac.ebi.pride.proteinidentificationindex.search.service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.repository.*;
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
    public static final float TERM_BOOST = 10;
    public static final float SECONDARY_TERM_BOOST = 0.5f;

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

    // Mapping query methods
    @Query("uniprot_mapping:?0")
    List<ProteinIdentification> findByUniprotMapping(String uniprotMapping);
    @Query("uniprot_mapping:(?0)")
    List<ProteinIdentification> findByUniprotMappingIn(Collection<String> uniprotMappings);
    @Query("ensembl_mapping:?0")
    List<ProteinIdentification> findByEnsemblMapping(String ensemblMapping);
    @Query("ensembl_mapping:(?0)")
    List<ProteinIdentification> findByEnsemblMappingIn(Collection<String> ensemblMappings);
    @Query("uniprot_mapping:?0 OR ensembl_mapping:?0")
    List<ProteinIdentification> findByUniprotMappingOrEnsemblMapping(String mapping);
    @Query("uniprot_mapping:(?0) OR ensembl_mapping:(?0)")
    List<ProteinIdentification> findByUniprotMappingOrEnsemblMappingIn(Collection<String> mappings);
    @Query("other_mappings:?0")
    List<ProteinIdentification> findByOtherMapping(String otherMapping);
    @Query("other_mappings:(?0)")
    List<ProteinIdentification> findByOtherMappingIn(Collection<String> otherMappings);
    @Query(value = "project_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR uniprot_mapping:?1 OR ensembl_mapping:?1 OR ambiguity_group:?1 OR other_mappings:?2)")
    List<ProteinIdentification> findByProjectAccessionAndAnyMapping(String projectAccession, @Boost(TERM_BOOST) String term, @Boost(SECONDARY_TERM_BOOST) String secondaryTerm);

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

    @Highlight(prefix = HIGHLIGHT_PRE_FRAGMENT, postfix = HIGHLIGHT_POST_FRAGMENT, fields = {"submitted_accession, accession, ambiguity_group, uniprot_mapping, ensembl_mapping, other_mappings"})
    @Query(value = "project_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR uniprot_mapping:?1 OR ensembl_mapping:?1 OR ambiguity_group:?1 OR other_mappings:?2)")
    HighlightPage<ProteinIdentification> findByProjectAccessionHighlights(String projectAccessions, @Boost(TERM_BOOST) String term, @Boost(SECONDARY_TERM_BOOST) String secondaryTerm, Pageable pageable);

    @Highlight(prefix = HIGHLIGHT_PRE_FRAGMENT, postfix = HIGHLIGHT_POST_FRAGMENT, fields = {"submitted_accession, accession, ambiguity_group, uniprot_mapping, ensembl_mapping, other_mappings"})
    @Query(value = "project_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR uniprot_mapping:?1 OR ensembl_mapping:?1 OR ambiguity_group:?1 OR other_mappings:?2)", filters = "mod_names:(?3)", defaultOperator = AND)
    HighlightPage<ProteinIdentification> findByProjectAccessionHighlightsAndFilterModNames(String projectAccessions, @Boost(TERM_BOOST) String term, @Boost(SECONDARY_TERM_BOOST) String secondaryTerm, List<String> modNameFilters, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "project_accession:?0")
    FacetPage<ProteinIdentification> findByProjectAccessionFacetModNames(String projectAccessions, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "project_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR uniprot_mapping:?1 OR ensembl_mapping:?1 OR ambiguity_group:?1 OR other_mappings:?2)")
    FacetPage<ProteinIdentification> findByProjectAccessionFacetModNames(String projectAccessions, @Boost(TERM_BOOST) String term, @Boost(SECONDARY_TERM_BOOST) String secondaryTerm, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "project_accession:?0", filters = "mod_names:(?1)", defaultOperator = AND)
    FacetPage<ProteinIdentification> findByProjectAccessionFacetModNamesAndFilterModNames(String projectAccessions, List<String> modNameFilters, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "project_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR uniprot_mapping:?1 OR ensembl_mapping:?1 OR ambiguity_group:?1 OR other_mappings:?2)", filters = "mod_names:(?3)", defaultOperator = AND)
    FacetPage<ProteinIdentification> findByProjectAccessionFacetModNamesAndFilterModNames(String projectAccessions, @Boost(TERM_BOOST) String term, @Boost(SECONDARY_TERM_BOOST) String secondaryTerm, List<String> modNameFilters, Pageable pageable);


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

    @Highlight(prefix = HIGHLIGHT_PRE_FRAGMENT, postfix = HIGHLIGHT_POST_FRAGMENT, fields = {"submitted_accession, accession, ambiguity_group, uniprot_mapping, ensembl_mapping, other_mappings"})
    @Query(value = "assay_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR uniprot_mapping:?1 OR ensembl_mapping:?1 OR ambiguity_group:?1 OR other_mappings:?2)")
    HighlightPage<ProteinIdentification> findByAssayAccessionHighlights(String assayAccession, @Boost(TERM_BOOST) String term, @Boost(SECONDARY_TERM_BOOST) String secondaryTerm, Pageable pageable);

    @Highlight(prefix = HIGHLIGHT_PRE_FRAGMENT, postfix = HIGHLIGHT_POST_FRAGMENT, fields = {"submitted_accession, accession, ambiguity_group, uniprot_mapping, ensembl_mapping, other_mappings"})
    @Query(value = "assay_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR uniprot_mapping:?1 OR ensembl_mapping:?1 OR ambiguity_group:?1 OR other_mappings:?2)", filters = "mod_names:(?3)", defaultOperator = AND)
    HighlightPage<ProteinIdentification> findByAssayAccessionHighlightsAndFilterModNames(String assayAccession, @Boost(TERM_BOOST) String term, @Boost(SECONDARY_TERM_BOOST) String secondaryTerm, List<String> modNameFilters, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "assay_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR uniprot_mapping:?1 OR ensembl_mapping:?1 OR ambiguity_group:?1 OR other_mappings:?2)")
    FacetPage<ProteinIdentification> findByAssayAccessionFacetModNames(String assayAccession, @Boost(TERM_BOOST) String term, @Boost(SECONDARY_TERM_BOOST) String secondaryTerm, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "assay_accession:?0")
    FacetPage<ProteinIdentification> findByAssayAccessionFacetModNames(String assayAccession, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "assay_accession:?0", filters = "mod_names:(?1)", defaultOperator = AND)
    FacetPage<ProteinIdentification> findByAssayAccessionFacetModNamesAndFilterModNames(String assayAccession, List<String> modNameFilters, Pageable pageable);

    @Facet(fields = {"mod_names"}, limit = 100)
    @Query(value = "assay_accession:?0 AND (submitted_accession:?1 OR accession:?1 OR uniprot_mapping:?1 OR ensembl_mapping:?1  OR ambiguity_group:?1 OR other_mappings:?2)", filters = "mod_names:(?3)", defaultOperator = AND)
    FacetPage<ProteinIdentification> findByAssayAccessionFacetModNamesAndFilterModNames(String assayAccession, @Boost(TERM_BOOST) String term, @Boost(SECONDARY_TERM_BOOST) String secondaryTerm, List<String> modNameFilters, Pageable pageable);


    // Submitted accession query methods
    @Query("submitted_accession:?0 AND assay_accession:?1")
    List<ProteinIdentification> findBySubmittedAccessionAndAssayAccession(String submittedAccession, String assayAccession);

    // Queries with Projections
    @Query(value = "project_accession:?0", fields = { "accession" })
    List<ProteinIdentificationSummary> findSummaryByProjectAccession(String projectAccession);

    @Query(value = "assay_accession:?0", fields = { "accession" })
    List<ProteinIdentificationSummary> findSummaryByAssayAccession(String assayAccession);

}
