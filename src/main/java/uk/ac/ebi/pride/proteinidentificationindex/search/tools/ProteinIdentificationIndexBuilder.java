package uk.ac.ebi.pride.proteinidentificationindex.search.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileProvider;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileSource;
import uk.ac.ebi.pride.archive.dataprovider.project.ProjectProvider;
import uk.ac.ebi.pride.archive.repo.assay.AssayRepository;
import uk.ac.ebi.pride.archive.repo.file.ProjectFileRepository;
import uk.ac.ebi.pride.archive.repo.project.ProjectRepository;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.proteincatalogindex.search.indexers.ProteinDetailsIndexer;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogIndexService;
import uk.ac.ebi.pride.proteincatalogindex.search.service.ProteinCatalogSearchService;
import uk.ac.ebi.pride.proteincatalogindex.search.util.ErrorLogOutputStream;
import uk.ac.ebi.pride.proteinidentificationindex.search.indexer.ProjectProteinIdentificationsIndexer;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationIndexService;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.ProteinIdentificationSearchService;

import java.io.File;
import java.util.Calendar;


/**
 * @author Jose A. Dianes
 * @version $Id$
 */
@Component
public class ProteinIdentificationIndexBuilder {

  private static Logger logger = LoggerFactory.getLogger(ProteinIdentificationIndexBuilder.class.getName());
  private static ErrorLogOutputStream errorLogOutputStream = new ErrorLogOutputStream(logger);

  private static final String PRIDE_MZ_TAB_FILE_EXTENSION = ".pride.mztab";

  private static final String COMPRESS_EXTENSION = "gz";

  private static final String INTERNAL_FOLDER_NAME = ProjectFileSource.INTERNAL.getFolderName();
  private static final String GENERATED_FOLDER_NAME = ProjectFileSource.GENERATED.getFolderName();

  @Autowired
  private File submissionsDirectory;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private AssayRepository assayRepository;

  @Autowired
  private ProjectFileRepository projectFileRepository;

  @Autowired
  private ProteinIdentificationSearchService proteinIdentificationSearchService;

  @Autowired
  private ProteinIdentificationIndexService proteinIdentificationIndexService;

  @Autowired
  private ProteinCatalogSearchService proteinCatalogSearchService;

  @Autowired
  private ProteinCatalogIndexService proteinCatalogIndexService;

  @Autowired
  private ProteinDetailsIndexer proteinCatalogDetailsIndexer;


  public static void main(String[] args) {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring/app-context.xml");
    ProteinIdentificationIndexBuilder proteinIdentificationIndexBuilder = context.getBean(ProteinIdentificationIndexBuilder.class);
    indexProteinIdentifications(proteinIdentificationIndexBuilder);
  }

  public static void indexProteinIdentifications(ProteinIdentificationIndexBuilder proteinIdentificationIndexBuilder) {
    Iterable<? extends ProjectProvider> projects = proteinIdentificationIndexBuilder.projectRepository.findAll();
    proteinIdentificationIndexBuilder.proteinIdentificationIndexService.deleteAll();
    logger.info("All Protein Identifications are now DELETED");
    ProjectProteinIdentificationsIndexer projectProteinIdentificationsIndexer =
        new ProjectProteinIdentificationsIndexer(proteinIdentificationIndexBuilder.proteinIdentificationSearchService,
            proteinIdentificationIndexBuilder.proteinIdentificationIndexService,
            proteinIdentificationIndexBuilder.proteinCatalogSearchService,
            proteinIdentificationIndexBuilder.proteinCatalogIndexService,
            proteinIdentificationIndexBuilder.proteinCatalogDetailsIndexer);
    for (ProjectProvider project : projects) {
      Iterable<? extends ProjectFileProvider> projectFiles = proteinIdentificationIndexBuilder.projectFileRepository.findAllByProjectId(project.getId());
      for (ProjectFileProvider projectFile : projectFiles) {
        if (ProjectFileSource.GENERATED.equals(projectFile.getFileSource())) {
          String fileName = projectFile.getFileName();
          if (fileName != null && fileName.contains(PRIDE_MZ_TAB_FILE_EXTENSION)) {
            String assayAccession = proteinIdentificationIndexBuilder.assayRepository.findOne(projectFile.getAssayId()).getAccession();
            String pathToMzTabFile = buildAbsoluteMzTabFilePath(
                proteinIdentificationIndexBuilder.submissionsDirectory.getAbsolutePath(),
                project,
                fileName
            );
            MZTabFileParser mzTabFileParser;
            try {
              logger.debug("Creating parser for MzTab file " + pathToMzTabFile);
              mzTabFileParser = new MZTabFileParser(new File(pathToMzTabFile), errorLogOutputStream);
              MZTabFile mzTabFile = mzTabFileParser.getMZTabFile();
              projectProteinIdentificationsIndexer.indexAllProteinIdentificationsForProjectAndAssay(project.getAccession(), assayAccession, mzTabFile);
            } catch (Exception e) {
              logger.error("Could not process MzTab file: " + pathToMzTabFile);
              logger.error("Cause: " + e.getCause());
            }
          }
        }
      }
    }
  }

  //TODO: Move it to a pride-archive-utils
  public static String buildAbsoluteMzTabFilePath(String prefix, ProjectProvider project, String fileName) {
    if (project.isPublicProject()) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(project.getPublicationDate());
      int month = calendar.get(Calendar.MONTH) + 1;

      return prefix
          + File.separator + calendar.get(Calendar.YEAR)
          + File.separator + (month < 10 ? "0" : "") + month
          + File.separator + project.getAccession()
          + File.separator + INTERNAL_FOLDER_NAME
          + File.separator + translateFromGeneratedToInternalFolderFileName(fileName);
    } else {
      return prefix
          + File.separator + project.getAccession()
          + File.separator + INTERNAL_FOLDER_NAME
          + File.separator + translateFromGeneratedToInternalFolderFileName(fileName);
    }
  }

  //TODO: Move it to a pride-archive-utils
  /**
   * In the generated folder(the which one we are taking the file names) the files are gzip, so we need to remove
   * the extension to have the name in the internal folder (the one that we want)
   *
   * @param fileName mztab file name in generated folder
   * @return mztab file name in internal folder
   */
  private static String translateFromGeneratedToInternalFolderFileName(String fileName) {

    if (fileName != null) {
      if (fileName.endsWith(COMPRESS_EXTENSION)) {
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
      }
    }
    return fileName;
  }
}
