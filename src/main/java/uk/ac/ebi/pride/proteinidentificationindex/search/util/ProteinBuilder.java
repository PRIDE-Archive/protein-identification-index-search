package uk.ac.ebi.pride.proteinidentificationindex.search.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinindex.search.model.ProteinIdentified;
import uk.ac.ebi.pride.proteinindex.search.util.ErrorLogOutputStream;
import uk.ac.ebi.pride.proteinindex.search.util.ProteinDetailUtils;
import uk.ac.ebi.pride.tools.protein_details_fetcher.ProteinDetailFetcher;
import uk.ac.ebi.pride.tools.utils.AccessionResolver;

import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProteinBuilder {

    private static final int PROCESSING_ACCESSIONS_STEP = 50;
    private static Logger logger = LoggerFactory.getLogger(uk.ac.ebi.pride.proteinindex.search.util.ProteinBuilder.class.getName());

    private static ErrorLogOutputStream errorLogOutputStream = new ErrorLogOutputStream(logger);


    public static List<ProteinIdentification> readProteinIdentificationsFromMzTabFile(String projectAccession, String assayAccession, MZTabFile tabFile) {

        List<ProteinIdentification> res = new LinkedList<ProteinIdentification>();

        if (tabFile != null) {
            // get proteins
            Collection<Protein> mzTabProteins = tabFile.getProteins();
            for (Protein mzTabProtein: mzTabProteins) {
                ProteinIdentification proteinIdentification = new ProteinIdentification();
                proteinIdentification.setId(ProteinIdentificationIdBuilder.getId(mzTabProtein.getAccession(), projectAccession, assayAccession));
                proteinIdentification.setSynonyms(new TreeSet<String>());
                proteinIdentification.setSubmittedAccession(mzTabProtein.getAccession());
                proteinIdentification.setAssayAccession(assayAccession);
                proteinIdentification.setProjectAccession(projectAccession);
                proteinIdentification.setAmbiguityGroupSubmittedAccessions(new LinkedList<String>());
                proteinIdentification.getAmbiguityGroupSubmittedAccessions().addAll(mzTabProtein.getAmbiguityMembers());
                // try to add a resolved accession to reference the catalog
                try {
                    String correctedAccession = getCorrectedAccession(mzTabProtein.getAccession(), mzTabProtein.getDatabase());
                    proteinIdentification.setAccession(correctedAccession);
                } catch (Exception e) {
                    logger.error("Cannot correct protein accession " + mzTabProtein.getAccession() + " with DB " + mzTabProtein.getDatabase());
                    logger.error("Original accession will be used");
                    logger.error("Cause:" + e.getCause());
                    proteinIdentification.setAccession(mzTabProtein.getAccession());
                }
                // add to final result
                res.add(proteinIdentification);

            }

            logger.debug("Found " + res.size() + " protein identifications for Assay " + assayAccession + " in file.");
        } else {
            logger.error("Passed null mzTab file to protein identifications reader");
        }

        return res;
    }

    public static void addProteinDetails(List<ProteinIdentified> proteins) {
        // build accession list to reduce the number of fetching requests
        List<String> accessions = new LinkedList<String>();
        for (ProteinIdentified protein: proteins) {
            accessions.add(protein.getAccession());
        }
        try {
            // get protein details (e.g. sequence, name)
            ProteinDetailFetcher proteinDetailFetcher = new ProteinDetailFetcher();

            HashMap<String, uk.ac.ebi.pride.tools.protein_details_fetcher.model.Protein> details = new HashMap<String, uk.ac.ebi.pride.tools.protein_details_fetcher.model.Protein>();

            int processedAccessions = 0;
            while (processedAccessions<accessions.size()) {
                // logging accessions
                String accessionListLog = new String();
                for (String accession: accessions.subList(processedAccessions, Math.min(accessions.size(),processedAccessions+PROCESSING_ACCESSIONS_STEP))) {
                    accessionListLog = accessionListLog + " <" + accession +">";
                }
                logger.debug("accession list is: " + accessionListLog);

                details.putAll(
                        proteinDetailFetcher.getProteinDetails(
                                accessions.subList(processedAccessions, Math.min(accessions.size(),processedAccessions+PROCESSING_ACCESSIONS_STEP))
                        )
                );

                logger.debug("Processed accessions: " + processedAccessions + " of " + accessions.size());
                logger.debug("Next step: " + (processedAccessions + PROCESSING_ACCESSIONS_STEP));
                logger.debug("Got details for up to " + details.size() + " accessions (cumulative)");

                processedAccessions = processedAccessions+PROCESSING_ACCESSIONS_STEP;
            }


            // add details to proteins
            for (ProteinIdentified protein: proteins) {
                if (details.containsKey(protein.getAccession())) {
                    if (details.get(protein.getAccession()).getSequenceString() != null) {
                        protein.setSequence(details.get(protein.getAccession()).getSequenceString());
                    }
                    if (details.get(protein.getAccession()).getName() != null) {
                        protein.setDescription(Arrays.asList(ProteinDetailUtils.NAME + details.get(protein.getAccession()).getName()));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot retrieve protein details for " + accessions.size() + " accessions.");
            e.printStackTrace();
        }
    }

    private static String getCorrectedAccession(String accession, String database) {

        try {
            AccessionResolver accessionResolver = new AccessionResolver(accession, null, database); // we don't have versions
            String fixedAccession = accessionResolver.getAccession();

            if (fixedAccession == null || "".equals(fixedAccession)) {
                logger.debug("No proper fix found for accession " + accession + ". Obtained: <" + fixedAccession +">. Original accession will be used.");
                return accession;
            } else {
                logger.debug("Original accession " + accession + " fixed to " + fixedAccession);
                return fixedAccession;
            }
        } catch (Exception e) {
            logger.error("There were problems getting corrected accession for " + accession +". Original accession will be used.");
            return accession;
        }
    }

}
