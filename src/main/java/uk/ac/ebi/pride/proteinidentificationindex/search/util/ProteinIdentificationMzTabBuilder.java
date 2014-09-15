package uk.ac.ebi.pride.proteinidentificationindex.search.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.indexutils.helpers.ModificationHelper;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.model.Modification;
import uk.ac.ebi.pride.jmztab.model.Protein;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.tools.utils.AccessionResolver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProteinIdentificationMzTabBuilder {

    private static Logger logger = LoggerFactory.getLogger(ProteinIdentificationMzTabBuilder.class.getName());

    public static String OPTIONAL_SEQUENCE_COLUMN   = "protein_sequence";

    public static List<ProteinIdentification> readProteinIdentificationsFromMzTabFile(String projectAccession, String assayAccession, MZTabFile tabFile) {

        List<ProteinIdentification> res = new LinkedList<ProteinIdentification>();

        if (tabFile != null) {
            // get proteins
            Collection<Protein> mzTabProteins = tabFile.getProteins();
            for (Protein mzTabProtein: mzTabProteins) {
                ProteinIdentification proteinIdentification = new ProteinIdentification();
                proteinIdentification.setId(ProteinIdentificationIdBuilder.getId(mzTabProtein.getAccession(), projectAccession, assayAccession));
                proteinIdentification.setOtherMappings(new TreeSet<String>());
                proteinIdentification.setSubmittedAccession(mzTabProtein.getAccession());
                proteinIdentification.setAssayAccession(assayAccession);
                proteinIdentification.setProjectAccession(projectAccession);
                proteinIdentification.setAmbiguityGroupSubmittedAccessions(new LinkedList<String>());

                //check if we have submitted sequence in mzTab and try to retrive it
                if(tabFile.getProteinColumnFactory().isOptionalColumn(OPTIONAL_SEQUENCE_COLUMN)){
                    String sequence = mzTabProtein.getOptionColumnValue(OPTIONAL_SEQUENCE_COLUMN);
                    if(sequence != null && sequence.isEmpty()){
                        proteinIdentification.setSubmittedSequence(sequence);
                        logger.debug("Retrieved submitted sequence");
                    }
                }
                if (mzTabProtein.getAmbiguityMembers()!=null && mzTabProtein.getAmbiguityMembers().size()>0 && !mzTabProtein.getAmbiguityMembers().get(0).equals("null")) {
                    proteinIdentification.getAmbiguityGroupSubmittedAccessions().addAll(mzTabProtein.getAmbiguityMembers());
                }
                proteinIdentification.setModifications(new LinkedList<ModificationProvider>());
                if (mzTabProtein.getModifications()!=null && mzTabProtein.getModifications().size()>0 && !mzTabProtein.getModifications().get(0).equals("null")) {
                    for (Modification mod: mzTabProtein.getModifications())
                    proteinIdentification.addModification(ModificationHelper.convertToModificationProvider(mod));
                }
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
