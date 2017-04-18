package uk.ac.ebi.pride.proteinidentificationindex.search.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.proteincatalogindex.search.indexers.ProteinDetailsIndexer;
import uk.ac.ebi.pride.proteincatalogindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogIndexService;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogSearchService;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinidentificationindex.search.util.ProteinIdentificationMzTabBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProjectProteinIdentificationsIndexer {

  private static Logger logger = LoggerFactory.getLogger(ProjectProteinIdentificationsIndexer.class.getName());

  private ProteinIdentificationSearchService proteinIdentificationSearchService;
  private ProteinIdentificationIndexService proteinIdentificationIndexService;

  private ProteinCatalogSearchService proteinCatalogSearchService;
  private ProteinCatalogIndexService proteinCatalogIndexService;
  private ProteinDetailsIndexer proteinCatalogDetailsIndexer;


  public ProjectProteinIdentificationsIndexer(ProteinIdentificationSearchService proteinIdentificationSearchService,
                                              ProteinIdentificationIndexService proteinIdentificationIndexService,
                                              ProteinCatalogSearchService proteinCatalogSearchService,
                                              ProteinCatalogIndexService proteinCatalogIndexService,
                                              ProteinDetailsIndexer proteinCatalogDetailsIndexer) {
    this.proteinIdentificationSearchService = proteinIdentificationSearchService;
    this.proteinIdentificationIndexService = proteinIdentificationIndexService;
    this.proteinCatalogSearchService = proteinCatalogSearchService;
    this.proteinCatalogIndexService = proteinCatalogIndexService;
    this.proteinCatalogDetailsIndexer = proteinCatalogDetailsIndexer;
  }

  public void indexAllProteinIdentificationsForProjectAndAssay(String projectAccession, String assayAccession, MZTabFile mzTabFile){
    try {  // build Protein Identifications from mzTabFile
      if (mzTabFile != null) {
        List<ProteinIdentification> proteinsFromFile = ProteinIdentificationMzTabBuilder.readProteinIdentificationsFromMzTabFile(projectAccession, assayAccession, mzTabFile);
        logger.debug("Found " + proteinsFromFile.size() + " Protein Identifications "
            + " for PROJECT:" + projectAccession
            + " and ASSAY:" + assayAccession);

        if (proteinsFromFile.size()>0) {
          addCatalogInfoToProteinIdentifications(proteinsFromFile);
          proteinIdentificationIndexService.save(proteinsFromFile);
          logger.debug("COMMITTED " + proteinsFromFile.size() +
              " Protein Identifications from PROJECT:" + projectAccession +
              " ASSAY:" + assayAccession);
        }
      } else {
        logger.error("An empty mzTab file has been passed to the indexing method - no indexing took place");
      }
    } catch (Exception e) {
      logger.error("Cannot index Protein Identifications from PROJECT:" + projectAccession + " and ASSAY:" + assayAccession );
      logger.error("Reason: ");
      e.printStackTrace();
    }
  }

  /**
   * Deletes all protein identifications for a given project accession
   *
   * @param projectAccession The accession that identifies the PRIDE Archive project
   */
  public void deleteAllProteinIdentificationsForProject(String projectAccession) {
    this.proteinIdentificationIndexService.delete(this.proteinIdentificationSearchService.findByProjectAccession(projectAccession));
  }

  /**
   * Use the protein Catalog to retrieve synonym information, protein details, etc.
   *
   * @param proteinIdentifications The list to be enriched with information from the Catalog
   */
  private void addCatalogInfoToProteinIdentifications(List<ProteinIdentification> proteinIdentifications) {
    List<ProteinIdentified> proteinsToCatalog = new LinkedList<>();
    for (ProteinIdentification proteinIdentification: proteinIdentifications) {
      List<ProteinIdentified> proteinsFromCatalog = proteinCatalogSearchService.findByAccession(proteinIdentification.getAccession());
      if (proteinsFromCatalog == null || proteinsFromCatalog.size() == 0) {
        logger.debug("Protein " + proteinIdentification.getAccession() + " not in the Catalog - adding...");
        proteinsToCatalog.addAll(getAsCatalogProtein(proteinIdentification));
      }
    }
    // if it happened that there were new proteins to the catalog, we need to save them (in batch)
    if (proteinsToCatalog.size()>0) {
      saveProteinsToCatalog(proteinsToCatalog);
    }
  }

  /**
   * This is a convenience method to convert from Protein Identification to Catalog model
   *
   * @param proteinIdentification The protein identification
   * @return A list of protein Catalog proteins
   */
  private List<ProteinIdentified> getAsCatalogProtein(ProteinIdentification proteinIdentification) {
    List<ProteinIdentification> pis = new LinkedList<>();
    pis.add(proteinIdentification);
    return getAsCatalogProtein(pis);
  }

  /**
   * This is a convenience method to convert from Protein Identification to Catalog model
   *
   * @param proteinIdentifications The list of protein identifications
   * @return A list of protein Catalog proteins
   */
  private List<ProteinIdentified> getAsCatalogProtein(List<ProteinIdentification> proteinIdentifications) {
    List<ProteinIdentified> result = new LinkedList<>();
    if (proteinIdentifications != null) {
      for (ProteinIdentification proteinIdentification: proteinIdentifications) {
        ProteinIdentified newPI = new ProteinIdentified();
        newPI.setAccession(proteinIdentification.getAccession());
        result.add(newPI);
      }
    }
    return result;
  }

  /**
   * Save proteins into the catalog, including adding synonyms and details
   * @param proteinsToCatalog
   */
  private void saveProteinsToCatalog(List<ProteinIdentified> proteinsToCatalog) {
    this.proteinCatalogIndexService.save(proteinsToCatalog);
    this.proteinCatalogDetailsIndexer.addMappingsToProteins(proteinsToCatalog);
    this.proteinCatalogDetailsIndexer.addDetailsToProteins(proteinsToCatalog);
  }
}
