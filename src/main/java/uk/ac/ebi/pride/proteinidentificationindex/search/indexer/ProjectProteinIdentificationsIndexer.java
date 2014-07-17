package uk.ac.ebi.pride.proteinidentificationindex.search.indexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.synonyms.ProteinAccessionSynonymsFinder;
import uk.ac.ebi.pride.proteinindex.search.util.ProteinBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProjectProteinIdentificationsIndexer {

    private static Logger logger = LoggerFactory.getLogger(ProjectProteinIdentificationsIndexer.class.getName());

    private ProteinIdentificationSearchService proteinIdentificationSearchService;
    private ProteinIdentificationIndexService proteinIdentificationIndexService;

    private uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService proteinCatalog;


    public ProjectProteinIdentificationsIndexer(ProteinIdentificationSearchService proteinIdentificationSearchService, ProteinIdentificationIndexService proteinIdentificationIndexService,
                                                uk.ac.ebi.pride.proteinindex.search.search.service.ProteinIdentificationSearchService proteinCatalog) {
        this.proteinIdentificationSearchService = proteinIdentificationSearchService;
        this.proteinIdentificationIndexService = proteinIdentificationIndexService;
        this.proteinCatalog = proteinCatalog;
    }


    public void indexAllProjectIdentificationsForProjectAndAssay(String projectAccession, String assayAccession, MZTabFile mzTabFile){
        List<ProteinIdentified> proteinsFromFile = new LinkedList<ProteinIdentified>();

        long startTime;
        long endTime;

        startTime = System.currentTimeMillis();

        // build Protein Identifications from mzTabFile
        try {
            if (mzTabFile != null) {
                proteinsFromFile = ProteinBuilder.readProteinIdentificationsFromMzTabFile(assayAccession, mzTabFile);
            }
        } catch (Exception e) { // we need to recover from any exception when reading the mzTab file so the whole process can continue
            logger.error("Cannot get Protein Identifications from PROJECT:" + projectAccession + "and ASSAY:" + assayAccession );
            logger.error("Reason: ");
            e.printStackTrace();
        }

        endTime = System.currentTimeMillis();
        logger.info("Found " + proteinsFromFile.size() + " Protein Identifications "
                + " for PROJECT:" + projectAccession
                + " and ASSAY:" + assayAccession
                + " in " + (double) (endTime - startTime) / 1000.0 + " seconds");

        if (proteinsFromFile != null && proteinsFromFile.size()>0) {

            startTime = System.currentTimeMillis();

            // convert to identifications model
            List<ProteinIdentification> proteinIdentifications = getAsProteinIdentifications(proteinsFromFile, projectAccession, assayAccession);
            addSynonymsToProteinIdentifications(proteinIdentifications);

            // save
            proteinIdentificationIndexService.save(proteinIdentifications);
            logger.debug("COMMITTED " + proteinsFromFile.size() +
                    " Protein Identifications from PROJECT:" + projectAccession +
                    " ASSAY:" + assayAccession);

            endTime = System.currentTimeMillis();
            logger.info("DONE indexing all Protein Identifications for project " + projectAccession + " in " + (double) (endTime - startTime) / 1000.0 + " seconds");
        }
    }

    /**
     * Use the protein Catalog to retrieve synonym information
     *
     * @param proteinIdentifications
     */
    private void addSynonymsToProteinIdentifications(List<ProteinIdentification> proteinIdentifications) {
        for (ProteinIdentification proteinIdentification: proteinIdentifications) {
            proteinIdentification.setSynonyms(new TreeSet<String>());
            List<ProteinIdentified> proteinsFromCatalog = proteinCatalog.findBySynonyms(proteinIdentification.getAccession());
            if (proteinsFromCatalog != null) {
                for (ProteinIdentified proteinFromCatalog: proteinsFromCatalog) {
                    proteinIdentification.getSynonyms().addAll(proteinFromCatalog.getSynonyms());
                }
            }
        }
    }

    private Set<String> getAccessionSet(List<ProteinIdentified> proteinsFromFile) {

        Set<String> res = new TreeSet<String>();

        for (ProteinIdentified proteinIdentified: proteinsFromFile) {
            res.add(proteinIdentified.getAccession());
        }

        return res;
    }

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

    public void deleteAllProteinIdentificationsForProject(String projectAccession) {
        // search by project accession
        List<ProteinIdentification> proteinIdentifications = this.proteinIdentificationSearchService.findByProjectAccession(projectAccession);
        this.proteinIdentificationIndexService.delete(proteinIdentifications);
    }

}
