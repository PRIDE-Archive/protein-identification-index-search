package uk.ac.ebi.pride.proteinidentificationindex.search.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinindex.search.indexers.ProteinDetailsIndexer;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.util.ProteinBuilder;

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
        this.indexAllProteinIdentificationsForProjectAndAssay(projectAccession,assayAccession,mzTabFile);
    }

    public void indexAllProteinIdentificationsForProjectAndAssay(String projectAccession, String assayAccession, MZTabFile mzTabFile){

        // build Protein Identifications from mzTabFile
        try {
            if (mzTabFile != null) {
                List<ProteinIdentified> proteinsFromFile = ProteinBuilder.readProteinIdentificationsFromMzTabFile(assayAccession, mzTabFile);

                logger.debug("Found " + proteinsFromFile.size() + " Protein Identifications "
                        + " for PROJECT:" + projectAccession
                        + " and ASSAY:" + assayAccession);

                if (proteinsFromFile!=null && proteinsFromFile.size()>0) {
                    // convert to identifications model
                    List<ProteinIdentification> proteinIdentifications = getAsProteinIdentifications(proteinsFromFile, projectAccession, assayAccession);
                    // add synonyms, details, etc
                    addCatalogInfoToProteinIdentifications(proteinIdentifications);
                    // save
                    proteinIdentificationIndexService.save(proteinIdentifications);

                    logger.debug("COMMITTED " + proteinsFromFile.size() +
                            " Protein Identifications from PROJECT:" + projectAccession +
                            " ASSAY:" + assayAccession);
                }
            } else {
                logger.error("An empty mzTab file has been passed to the indexing method - no indexing took place");
            }
        } catch (Exception e) { // we need to recover from any exception when reading the mzTab file so the whole process can continue
            logger.error("Cannot get Protein Identifications from PROJECT:" + projectAccession + "and ASSAY:" + assayAccession );
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
        for (ProteinIdentification proteinIdentification: proteinIdentifications) {
            proteinIdentification.setSynonyms(new TreeSet<String>());
            proteinIdentification.setDescription(new LinkedList<String>());
            List<ProteinIdentified> proteinsFromCatalog = proteinCatalogSearchService.findBySynonyms(proteinIdentification.getAccession());
            if (proteinsFromCatalog != null && proteinsFromCatalog.size()>0) {
                logger.debug("Protein " + proteinIdentification.getAccession() + " already in the Catalog - getting details...");
                for (ProteinIdentified proteinFromCatalog: proteinsFromCatalog) {
                    proteinIdentification.getSynonyms().addAll(proteinFromCatalog.getSynonyms());
                    proteinIdentification.getDescription().addAll(proteinFromCatalog.getDescription());
                    proteinIdentification.setSequence(proteinFromCatalog.getSequence());
                }
            } else { // if not present, we need to update the catalog
                logger.debug("Protein " + proteinIdentification.getAccession() + " not in the Catalog - adding...");
                List<ProteinIdentified> proteinsToCatalog = getAsCatalogProtein(proteinIdentification);
                this.proteinCatalogIndexService.save(proteinsToCatalog);
                this.proteinCatalogDetailsIndexer.addSynonymsToProteins(proteinsToCatalog);
                this.proteinCatalogDetailsIndexer.addDetailsToProteins(proteinsToCatalog);
                // add details to identifications
                proteinsFromCatalog = this.proteinCatalogSearchService.findBySynonyms(proteinIdentification.getAccession());
                if (proteinsFromCatalog != null && proteinsFromCatalog.size()>0) {
                    logger.debug("Obtained " + proteinsFromCatalog.size() + " from the Catalog after saving");
                    for (ProteinIdentified proteinFromCatalog : proteinsFromCatalog) {
                        if (proteinFromCatalog.getSynonyms()!=null) proteinIdentification.getSynonyms().addAll(proteinFromCatalog.getSynonyms());
                        if (proteinFromCatalog.getDescription()!=null) proteinIdentification.getDescription().addAll(proteinFromCatalog.getDescription());
                        proteinIdentification.setSequence(proteinFromCatalog.getSequence());
                    }
                } else {
                    logger.error("Obtained NO protein from the Catalog after saving - there were problems!");
                }
            }
        }
    }

    /**
     * This is a convenience method to convert from Catalog model to Protein Identification model. Basically adds
     * project and assay accessions.
     *
     * @param proteinsIdentified The list of Catalog proteins
     * @param projectAccession The accession that identifies the PRIDE Archive project
     * @param assayAccession The accession that identifies the PRIDE Archive assay
     * @return A list of protein identifications
     */
    private List<ProteinIdentification> getAsProteinIdentifications(List<ProteinIdentified> proteinsIdentified, String projectAccession, String assayAccession) {
        if (proteinsIdentified != null) {
            List<ProteinIdentification> res = new LinkedList<ProteinIdentification>();
            for (ProteinIdentified proteinIdentified: proteinsIdentified) {
                ProteinIdentification newPI = new ProteinIdentification();
                newPI.setAccession(proteinIdentified.getAccession());
                newPI.setProjectAccession(projectAccession);
                newPI.setAssayAccession(assayAccession);
                newPI.setSequence(proteinIdentified.getSequence());
                newPI.setSynonyms(proteinIdentified.getSynonyms());
                newPI.setDescription(proteinIdentified.getDescription());
                res.add(newPI);
            }
            return res;
        } else {
            return null;
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
                newPI.setProjectAccessions(new TreeSet<String>()); // TODO: not needed in the future catalog
                newPI.setAssayAccessions(new TreeSet<String>()); // TODO: not needed in the future catalog
                res.add(newPI);
            }
            return res;
        } else {
            return null;
        }
    }

}
