package uk.ac.ebi.pride.proteinidentificationindex.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.FacetFieldEntry;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.indexutils.results.PageWrapper;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentificationFields;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentificationSummary;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.repository.SolrProteinIdentificationRepository;

import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *          <p/>
 *          NOTE: protein accessions can contain chars that produce problems in solr queries ([,],:). They are replaced by _ when
 *          using the repository methods
 */
@SuppressWarnings("unused")
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
  public Long countByProjectAccession(String projectAccession) {
    return solrProteinIdentificationRepository.countByProjectAccession(projectAccession);
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

  public List<ProteinIdentification> findByProjectAccessionAndAccession(String projectAccession, String accession) {
    return solrProteinIdentificationRepository.findByProjectAccessionAndAccession(projectAccession, accession);
  }

  public Page<ProteinIdentification> findByProjectAccessionAndAccession(String projectAccession, String accession, Pageable pageable) {
    return solrProteinIdentificationRepository.findByProjectAccessionAndAccession(projectAccession, accession, pageable);
  }

  public Long countByProjectAccessionAndAccession(String projectAccession, String accession) {
    return solrProteinIdentificationRepository.countByProjectAccessionAndAccession(projectAccession, accession);
  }

  // Assay accession query methods
  public List<ProteinIdentification> findByAssayAccession(String assayAccession) {
    return solrProteinIdentificationRepository.findByAssayAccession(assayAccession);
  }
  public Long countByAssayAccession(String assayAccession) {
    return solrProteinIdentificationRepository.countByAssayAccession(assayAccession);
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

  public List<ProteinIdentification> findByAssayAccessionAndAccession(String assayAccession, String accession) {
    return solrProteinIdentificationRepository.findByAssayAccessionAndAccession(assayAccession, accession);
  }

  public Page<ProteinIdentification> findByAssayAccessionAndAccession(String assayAccession, String accession, Pageable pageable) {
    return solrProteinIdentificationRepository.findByAssayAccessionAndAccession(assayAccession, accession, pageable);
  }

  public Long countByAssayAccessionAndAccession(String assayAccession, String accession) {
    return solrProteinIdentificationRepository.countByAssayAccessionAndAccession(assayAccession, accession);
  }

  /**
   * Count the facets per modification name
   * @param assayAccession mandatory
   * @param term optional
   * @param modNameFilters optional
   * @return a map with the mod_names and the number of hits per mod_synonym
   */
  public Map<String, Long> findByAssayAccessionFacetOnModificationNames(String assayAccession, String term, List<String> modNameFilters) {
    Map<String, Long> modificationsCount = new TreeMap<>();
    FacetPage<ProteinIdentification> proteinIdentifications;
    if ((term == null || term.isEmpty()) && (modNameFilters == null || modNameFilters.isEmpty())) {
      proteinIdentifications = solrProteinIdentificationRepository.findByAssayAccessionFacetModNames(assayAccession, new PageRequest(0, 1));
    } else if ((term != null && !term.isEmpty()) && (modNameFilters == null || modNameFilters.isEmpty())) {
      proteinIdentifications = solrProteinIdentificationRepository.findByAssayAccessionFacetModNames(assayAccession, term, term, new PageRequest(0, 1));
    } else if ((term == null || term.isEmpty()) && (modNameFilters != null && !modNameFilters.isEmpty())) {
      proteinIdentifications = solrProteinIdentificationRepository.findByAssayAccessionFacetModNamesAndFilterModNames(assayAccession, modNameFilters, new PageRequest(0, 1));
    } else {
      proteinIdentifications = solrProteinIdentificationRepository.findByAssayAccessionFacetModNamesAndFilterModNames(assayAccession, term, term, modNameFilters, new PageRequest(0, 1));
    }
    if (proteinIdentifications != null) {
      for (FacetFieldEntry facetFieldEntry : proteinIdentifications.getFacetResultPage(ProteinIdentificationFields.MOD_NAMES)) {
        modificationsCount.put(facetFieldEntry.getValue(), facetFieldEntry.getValueCount());
      }
    }
    return modificationsCount;
  }

  /**
   * Count the facets per modification name
   * @param projectAccession mandatory
   * @param term optional
   * @param modNameFilters optional
   * @return a map with the mod_names and the number of hits per mod_synonym
   */
  public Map<String, Long> findByProjectAccessionFacetOnModificationNames(String projectAccession, String term, List<String> modNameFilters) {
    Map<String, Long> modificationsCount = new TreeMap<>();
    FacetPage<ProteinIdentification> proteinIdentifications;
    if ((term == null || term.isEmpty()) && (modNameFilters == null || modNameFilters.isEmpty())) {
      proteinIdentifications = solrProteinIdentificationRepository.findByProjectAccessionFacetModNames(projectAccession, new PageRequest(0, 1));
    } else if ((term != null && !term.isEmpty()) && (modNameFilters == null || modNameFilters.isEmpty())) {
      proteinIdentifications = solrProteinIdentificationRepository.findByProjectAccessionFacetModNames(projectAccession, term, new PageRequest(0, 1));
    } else if ((term == null || term.isEmpty()) && (modNameFilters != null && !modNameFilters.isEmpty())) {
      proteinIdentifications = solrProteinIdentificationRepository.findByProjectAccessionFacetModNamesAndFilterModNames(projectAccession, modNameFilters, new PageRequest(0, 1));
    } else {
      proteinIdentifications = solrProteinIdentificationRepository.findByProjectAccessionFacetModNamesAndFilterModNames(projectAccession, term, modNameFilters, new PageRequest(0, 1));
    }
    if (proteinIdentifications != null) {
      for (FacetFieldEntry facetFieldEntry : proteinIdentifications.getFacetResultPage(ProteinIdentificationFields.MOD_NAMES)) {
        modificationsCount.put(facetFieldEntry.getValue(), facetFieldEntry.getValueCount());
      }
    }
    return modificationsCount;
  }

  /**
   * Return protein identifications filtered (or not) by modifications names with the highlights for modification names
   *
   * @param assayAccession mandatory
   * @param term optional
   * @param modNameFilters optional
   * @param pageable requested page
   * @return A page with the protein identifications and the highlights snippets
   */
  public PageWrapper<ProteinIdentification> findByAssayAccessionHighlightsOnModificationNames(
      String assayAccession, String term, List<String> modNameFilters, Pageable pageable) {
    PageWrapper<ProteinIdentification> proteinIdentifications;
    if ((term == null || term.isEmpty()) && (modNameFilters == null || modNameFilters.isEmpty())) {
      proteinIdentifications = new PageWrapper<>(solrProteinIdentificationRepository.findByAssayAccession(assayAccession, pageable));
    } else if ((term != null && !term.isEmpty()) && (modNameFilters == null || modNameFilters.isEmpty())) {
      proteinIdentifications = new PageWrapper<>(solrProteinIdentificationRepository.findByAssayAccessionHighlights(assayAccession, term, term, pageable));
    } else if ((term == null || term.isEmpty()) && (modNameFilters != null && !modNameFilters.isEmpty())) {
      proteinIdentifications = new PageWrapper<>(solrProteinIdentificationRepository.findByAssayAccessionAndFilterModNames(assayAccession, modNameFilters, pageable));
    } else {
      proteinIdentifications = new PageWrapper<>(solrProteinIdentificationRepository.findByAssayAccessionHighlightsAndFilterModNames(assayAccession, term, term, modNameFilters, pageable));
    }
    return proteinIdentifications;
  }

  /**
   * Return protein identifications filtered (or not) by modifications names with the highlights for modification names
   *
   * @param projectAccession mandatory
   * @param term optional
   * @param modNameFilters optional
   * @param pageable requested page
   * @return A page with the protein identifications and the highlights snippets
   */
  public PageWrapper<ProteinIdentification> findByProjectAccessionHighlightsOnModificationNames(
      String projectAccession, String term, List<String> modNameFilters, Pageable pageable) {
    PageWrapper<ProteinIdentification> proteinIdentifications;
    if ((term == null || term.isEmpty()) && (modNameFilters == null || modNameFilters.isEmpty())) {
      proteinIdentifications = new PageWrapper<>(solrProteinIdentificationRepository.findByProjectAccession(projectAccession, pageable));
    } else if ((term != null && !term.isEmpty()) && (modNameFilters == null || modNameFilters.isEmpty())) {
      proteinIdentifications = new PageWrapper<>(solrProteinIdentificationRepository.findByProjectAccessionHighlights(projectAccession, term, pageable));
    } else if ((term == null || term.isEmpty()) && (modNameFilters != null && !modNameFilters.isEmpty())) {
      proteinIdentifications = new PageWrapper<>(solrProteinIdentificationRepository.findByProjectAccessionAndFilterModNames(projectAccession, modNameFilters, pageable));
    } else {
      proteinIdentifications = new PageWrapper<>(solrProteinIdentificationRepository.findByProjectAccessionHighlightsAndFilterModNames(projectAccession, term, modNameFilters, pageable));
    }
    return proteinIdentifications;
  }

  public List<ProteinIdentificationSummary> findSummaryByProjectAccession(String projectAccession) {
    return solrProteinIdentificationRepository.findSummaryByProjectAccession(projectAccession);
  }

  public Set<String> getUniqueProteinAccessionsByProjectAccession(String projectAccession) {
    List<ProteinIdentificationSummary> results = findSummaryByProjectAccession(projectAccession);
    Set<String> accessions = new HashSet<>(results.size());
    for (ProteinIdentificationSummary result : results) {
      ProteinIdentification ident = (ProteinIdentification) result; // need to cast to the real bean before accessing data
      accessions.add(ident.getAccession());
    }
    return accessions;
  }

  public List<ProteinIdentificationSummary> findSummaryByAssayAccession(String assayAccession) {
    return solrProteinIdentificationRepository.findSummaryByAssayAccession(assayAccession);
  }

  public Set<String> getUniqueProteinAccessionsByAssayAccession(String assayAccession) {
    List<ProteinIdentificationSummary> results = findSummaryByAssayAccession(assayAccession);
    Set<String> accessions = new HashSet<>(results.size());
    for (ProteinIdentificationSummary result : results) {
      ProteinIdentification ident = (ProteinIdentification) result; // need to cast to the real bean before accessing data
      accessions.add(ident.getAccession());
    }
    return accessions;
  }
}
