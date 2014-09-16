package uk.ac.ebi.pride.proteinidentificationindex.search.model;

import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinDetailProvider;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinIdentificationProvider;
import uk.ac.ebi.pride.indexutils.helpers.ModificationHelper;
import uk.ac.ebi.pride.proteincatalogindex.search.util.ProteinDetailUtils;

import java.util.*;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProteinIdentification implements ProteinIdentificationProvider, ProteinDetailProvider {

    @Field(ProteinIdentificationFields.ID)
    private String id;

    @Field(ProteinIdentificationFields.SUBMITTED_ACCESSION)
    private String submittedAccession;

    @Field(ProteinIdentificationFields.ACCESSION)
    private String accession;

    @Field(ProteinIdentificationFields.UNIPROT_MAPPING)
    private String uniprotMapping;

    @Field(ProteinIdentificationFields.ENSEMBL_MAPPING)
    private String ensemblMapping;

    @Field(ProteinIdentificationFields.OTHER_MAPPINGS)
    private Set<String> otherMappings;

    @Field(ProteinIdentificationFields.SUBMITTED_SEQUENCE)
    private String submittedSequence;

    @Field(ProteinIdentificationFields.INFERRED_SEQUENCE)
    private String inferredSequence;

    @Field(ProteinIdentificationFields.DESCRIPTION)
    private List<String> description;

    @Field(ProteinIdentificationFields.AMBIGUITY_GROUP)
    private List<String> ambiguityGroupSubmittedAccessions;

    @Field(ProteinIdentificationFields.MODIFICATIONS)
    private List<String> modificationsAsString;

    @Field(ProteinIdentificationFields.MOD_NAMES)
    private List<String> modificationNames;

    @Field(ProteinIdentificationFields.MOD_ACCESSIONS)
    private List<String> modificationAccessions;

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

    public String getSubmittedAccession() {
        return submittedAccession;
    }

    public void setSubmittedAccession(String submittedAccession) {
        this.submittedAccession = submittedAccession;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    @Override
    public String getName() {
        return ProteinDetailUtils.getNameFromDescription(description);
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

    public String getSubmittedSequence() {
        return submittedSequence;
    }

    public void setSubmittedSequence(String submittedSequence) {
        this.submittedSequence = submittedSequence;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public List<String> getAmbiguityGroupSubmittedAccessions() {
        return ambiguityGroupSubmittedAccessions;
    }

    public void setAmbiguityGroupSubmittedAccessions(List<String> ambiguityGroupSubmittedAccessions) {
        this.ambiguityGroupSubmittedAccessions = ambiguityGroupSubmittedAccessions;
    }

    public Iterable<ModificationProvider> getModifications() {

        List<ModificationProvider> modifications = new ArrayList<ModificationProvider>();

        if (modificationsAsString != null) {
            for (String mod : modificationsAsString) {
                if(!mod.isEmpty()) {
                    modifications.add(ModificationHelper.convertFromString(mod));
                }
            }
        }

        return modifications;
    }

    public void setModifications(List<ModificationProvider> modifications) {

        if (modifications == null)
            return;

        List<String> modificationsAsString = new ArrayList<String>();
        List<String> modificationNames = new ArrayList<String>();
        List<String> modificationAccessions = new ArrayList<String>();

        for (ModificationProvider modification : modifications) {
            modificationsAsString.add(ModificationHelper.convertToString(modification));
            modificationAccessions.add(modification.getAccession());
            modificationNames.add(modification.getName());
        }

        this.modificationsAsString = modificationsAsString;
        this.modificationAccessions = modificationAccessions;
        this.modificationNames = modificationNames;
    }

    public void addModification(ModificationProvider modification) {

        if (modificationsAsString == null) {
            modificationsAsString = new ArrayList<String>();
        }

        if (modificationAccessions == null) {
            modificationAccessions = new ArrayList<String>();
        }

        if (modificationNames == null) {
            modificationNames = new ArrayList<String>();
        }


        modificationsAsString.add(ModificationHelper.convertToString(modification));
        modificationAccessions.add(modification.getAccession());
        modificationNames.add(modification.getName());
    }

    public Set<String> getModificationNames() {

        Set<String> modificationNamesSet = new TreeSet<String>();
        if (modificationNames != null) {
            modificationNamesSet.addAll(modificationNames);
        }
        return modificationNamesSet;
    }

    public Set<String> getModificationAccessions() {

        Set<String> modificationAccessionsSet = new TreeSet<String>();
        if (modificationAccessions != null) {
            modificationAccessionsSet.addAll(modificationAccessions);
        }
        return modificationAccessionsSet;
    }

    public String getUniprotMapping() {
        return uniprotMapping;
    }

    public void setUniprotMapping(String uniprotMapping) {
        this.uniprotMapping = uniprotMapping;
    }

    public String getEnsemblMapping() {
        return ensemblMapping;
    }

    public void setEnsemblMapping(String ensemblMapping) {
        this.ensemblMapping = ensemblMapping;
    }

    public Set<String> getOtherMappings() {
        return otherMappings;
    }

    public void setOtherMappings(Set<String> otherMappings) {
        this.otherMappings = otherMappings;
    }

    public String getInferredSequence() {
        return inferredSequence;
    }

    public void setInferredSequence(String inferredSequence) {
        this.inferredSequence = inferredSequence;
    }
}
