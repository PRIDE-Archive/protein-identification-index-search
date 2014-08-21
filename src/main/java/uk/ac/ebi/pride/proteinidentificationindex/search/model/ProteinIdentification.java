package uk.ac.ebi.pride.proteinidentificationindex.search.model;

import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ebi.pride.archive.dataprovider.identification.ModificationProvider;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinDetailProvider;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinIdentificationProvider;
import uk.ac.ebi.pride.indexutils.helpers.ModificationHelper;
import uk.ac.ebi.pride.indexutils.modifications.Modification;
import uk.ac.ebi.pride.proteinindex.search.util.ProteinDetailUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Field(ProteinIdentificationFields.PROJECT_ACCESSION)
    private String projectAccession;

    @Field(ProteinIdentificationFields.ASSAY_ACCESSION)
    private String assayAccession;

    @Field(ProteinIdentificationFields.SYNONYMS)
    private Set<String> synonyms;

    @Field(ProteinIdentificationFields.SEQUENCE)
    private String sequence;

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

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
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

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
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

}
