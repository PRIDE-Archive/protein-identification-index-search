package uk.ac.ebi.pride.proteinidentificationindex.search.model;

import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.indexutils.helpers.ModificationHelper;

import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProteinIdentification {

  @Field(ProteinIdentificationFields.ID)
  private String id;

  @Field(ProteinIdentificationFields.ACCESSION)
  private String accession;

  @Field(ProteinIdentificationFields.MOD_NAMES)
  private List<String> modificationNames;
  @Field(ProteinIdentificationFields.PROJECT_ACCESSION)
  private String projectAccession;

  @Field(ProteinIdentificationFields.ASSAY_ACCESSION)
  private String assayAccession;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAccession() {
    return accession;
  }

  public void setAccession(String accession) {
    this.accession = accession;
  }

  public String getProjectAccession() {
    return projectAccession;
  }

  public void setProjectAccession(String projectAccession) {
    this.projectAccession = projectAccession;
  }

  public String getAssayAccession() {
    return assayAccession;
  }

  public void setAssayAccession(String assayAccession) {
    this.assayAccession = assayAccession;
  }

  public Iterable<String> getModificationNames() {
    return this.modificationNames;
  }

  public void setModificationNames(List<ModificationProvider> modifications) {
    this.modificationNames = new ArrayList<>();
    if (modifications == null && modifications.size()>0) {
      for (ModificationProvider modification : modifications) {
        modificationNames.add(modification.getName());
      }
    }
  }

  public void addModificationName(ModificationProvider modification) {
    if (modificationNames == null) {
      modificationNames = new ArrayList<>();
    }
    if (modification!=null) {
      modificationNames.add(modification.getName());
    }
  }
}
