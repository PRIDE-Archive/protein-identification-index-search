package uk.ac.ebi.pride.proteinidentificationindex.search.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinidentificationindex.search.util.ProteinIdentificationMzTabBuilder;
import uk.ac.ebi.pride.proteinindex.search.indexers.ProteinDetailsIndexer;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;

import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProjectProteinIdentificationsIndexer {

    private static Logger logger = LoggerFactory.getLogger(ProjectProteinIdentificationsIndexer.class.getName());

    private ProteinIdentificationSearchService proteinIdentificationSearchService;
    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    private uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService proteinCatalogSearchService;
    private uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationIndexService proteinCatalogIndexService;
    private ProteinDetailsIndexer proteinCatalogDetailsIndexer;


    public ProjectProteinIdentificationsIndexer(ProteinIdentificationSearchService proteinIdentificationSearchService, ProteinIdentificationIndexService proteinIdentificationIndexService,
                                                uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService proteinCatalogSearchService,
                                                uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationIndexService proteinCatalogIndexService,
                                                ProteinDetailsIndexer proteinCatalogDetailsIndexer) {
        this.proteinIdentificationSearchService = proteinIdentificationSearchService;
        this.proteinIdentificationIndexService = proteinIdentificationIndexService;
        this.proteinCatalogSearchService = proteinCatalogSearchService;
        this.proteinCatalogIndexService = proteinCatalogIndexService;
        this.proteinCatalogDetailsIndexer = proteinCatalogDetailsIndexer;
    }

    @Deprecated
    public void indexAllProjectIdentificationsForProjectAndAssay(String projectAccession, String assayAccession, MZTabFile mzTabFile){
        this.indexAllProteinIdentificationsForProjectAndAssay(projectAccession, assayAccession, mzTabFile);
    }

    public void indexAllProteinIdentificationsForProjectAndAssay(String projectAccession, String assayAccession, MZTabFile mzTabFile){

        // build Protein Identifications from mzTabFile
        try {
            if (mzTabFile != null) {
                List<ProteinIdentification> proteinsFromFile = ProteinIdentificationMzTabBuilder.readProteinIdentificationsFromMzTabFile(projectAccession, assayAccession, mzTabFile);

                logger.debug("Found " + proteinsFromFile.size() + " Protein Identifications "
                        + " for PROJECT:" + projectAccession
                        + " and ASSAY:" + assayAccession);

                if (proteinsFromFile!=null && proteinsFromFile.size()>0) {
                    // add synonyms, details, etc
                    addCatalogInfoToProteinIdentifications(proteinsFromFile);
                    // save
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
        // search by project accession
        List<ProteinIdentification> proteinIdentifications = this.proteinIdentificationSearchService.findByProjectAccession(projectAccession);
        this.proteinIdentificationIndexService.delete(proteinIdentifications);
    }

    /**
     * Use the protein Catalog to retrieve synonym information, protein details, etc.
     *
     * @param proteinIdentifications The list to be enriched with information from the Catalog
     */
    private void addCatalogInfoToProteinIdentifications(List<ProteinIdentification> proteinIdentifications) {
        List<ProteinIdentified> proteinsToCatalog = new LinkedList<ProteinIdentified>();

        // a first round, will identify new proteins for the catalog
        // we do it this way, since we want to save to the catalog in batches (to improve performance)
        // if we process identifications and catalog at the same time, we have to save to the catalog once by one
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

        // now we are save to assume that the catalog contains all the identified proteins
        for (ProteinIdentification proteinIdentification: proteinIdentifications) {
            proteinIdentification.setOtherMappings(new TreeSet<String>());
            proteinIdentification.setDescription(new LinkedList<String>());
            List<ProteinIdentified> proteinsFromCatalog = proteinCatalogSearchService.findByAccession(proteinIdentification.getAccession());
            if (proteinsFromCatalog != null && proteinsFromCatalog.size() > 0) {
                logger.debug("Protein " + proteinIdentification.getAccession() + " already in the Catalog - getting details...");
                for (ProteinIdentified proteinFromCatalog : proteinsFromCatalog) {
                    updateProteinIdentification(proteinIdentification, proteinFromCatalog);
                }
            } else { // if not present, there were errors...
                logger.error("Protein " + proteinIdentification.getId() + " not in the catalog - It should be saved by now...");
            }
        }

    }

    /**
     * This is a convenience method to convert from Protein Identification to Catalog model
     *
     * @param proteinIdentification The protein identification
     * @return A list of protein Catalog proteins
     */
    private List<ProteinIdentified> getAsCatalogProtein(ProteinIdentification proteinIdentification) {
        List<ProteinIdentification> pis = new LinkedList<ProteinIdentification>();
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
        if (proteinIdentifications != null) {
            List<ProteinIdentified> res = new LinkedList<ProteinIdentified>();
            for (ProteinIdentification proteinIdentification: proteinIdentifications) {
                ProteinIdentified newPI = new ProteinIdentified();
                newPI.setAccession(proteinIdentification.getAccession());
                res.add(newPI);
            }
            return res;
        } else {
            return null;
        }
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

    private void updateProteinIdentification(ProteinIdentification proteinIdentification, ProteinIdentified proteinFromCatalog) {
        if (proteinFromCatalog.getUniprotMapping()!=null) proteinIdentification.setUniprotMapping(proteinFromCatalog.getUniprotMapping());
        if (proteinFromCatalog.getEnsemblMapping()!=null) proteinIdentification.setEnsemblMapping(proteinFromCatalog.getEnsemblMapping());
        if (proteinFromCatalog.getOtherMappings() != null) proteinIdentification.getOtherMappings().addAll(proteinFromCatalog.getOtherMappings());
        if (proteinFromCatalog.getDescription() != null) proteinIdentification.getDescription().addAll(proteinFromCatalog.getDescription());
        proteinIdentification.setInferredSequence(proteinFromCatalog.getInferredSequence());
    }


}
