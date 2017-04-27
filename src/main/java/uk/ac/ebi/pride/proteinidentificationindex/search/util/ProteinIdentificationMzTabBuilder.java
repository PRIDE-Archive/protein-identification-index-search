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

import java.util.*;

public class ProteinIdentificationMzTabBuilder {

  private static Logger logger = LoggerFactory.getLogger(ProteinIdentificationMzTabBuilder.class.getName());

  public static String OPTIONAL_SEQUENCE_COLUMN   = "protein_sequence";

  public static List<ProteinIdentification> readProteinIdentificationsFromMzTabFile(String projectAccession, String assayAccession, MZTabFile tabFile) {
    List<ProteinIdentification> result = new LinkedList<>();
    String sequence;
    if (tabFile != null) {
      Collection<Protein> mzTabProteins = tabFile.getProteins();
      for (Protein mzTabProtein: mzTabProteins) {
        ProteinIdentification proteinIdentification = new ProteinIdentification();
        proteinIdentification.setId(ProteinIdentificationIdBuilder.getId(mzTabProtein.getAccession(), projectAccession, assayAccession));
        proteinIdentification.setAssayAccession(assayAccession);
        proteinIdentification.setProjectAccession(projectAccession);
        proteinIdentification.setModificationNames(new ArrayList<ModificationProvider>());
        if (mzTabProtein.getModifications()!=null && mzTabProtein.getModifications().size()>0 && !mzTabProtein.getModifications().get(0).toString().equalsIgnoreCase("null")) {
          for (Modification mod: mzTabProtein.getModifications())
            proteinIdentification.addModificationName(ModificationHelper.convertToModificationProvider(mod));
        }
        try {  // try to add a resolved accession to reference the catalog
          String correctedAccession = getCorrectedAccession(mzTabProtein.getAccession(), mzTabProtein.getDatabase());
          proteinIdentification.setAccession(correctedAccession);
        } catch (Exception e) {
          logger.error("Cannot correct protein accession " + mzTabProtein.getAccession() + " with DB " + mzTabProtein.getDatabase());
          logger.error("Original accession will be used", e);
          proteinIdentification.setAccession(mzTabProtein.getAccession());
        }
        result.add(proteinIdentification);
      }
      logger.debug("Found " + result.size() + " protein identifications for Assay " + assayAccession + " in file.");
    } else {
      logger.error("Passed null mzTab file to protein identifications reader");
    }
    return result;
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
