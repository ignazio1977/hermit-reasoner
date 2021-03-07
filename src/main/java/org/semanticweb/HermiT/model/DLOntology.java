/* Copyright 2008, 2009, 2010 by the Oxford University Computing Laboratory

   This file is part of HermiT.

   HermiT is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   HermiT is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with HermiT.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.semanticweb.HermiT.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.HermiT.Prefixes;

/**
 * Represents a DL ontology as a set of rules.
 */
public class DLOntology implements Serializable {
    private static final long serialVersionUID=3189937959595369812L;
    protected static final String CRLF="\n";

    protected final String m_ontologyIRI;
    protected final Collection<DLClause> m_dlClauses;
    protected final Set<Atom> m_positiveFacts;
    protected final Set<Atom> m_negativeFacts;
    protected final boolean m_hasInverseRoles;
    protected final boolean m_hasAtMostRestrictions;
    protected final boolean m_hasNominals;
    protected final boolean m_hasDatatypes;
    protected final boolean m_isHorn;
    protected final Set<AtomicConcept> m_allAtomicConcepts;
    protected final int m_numberOfExternalConcepts;
    protected final Set<AtomicRole> m_allAtomicObjectRoles;
    protected final Set<Role> m_allComplexObjectRoles;
    protected final Set<AtomicRole> m_allAtomicDataRoles;
    protected final Set<DatatypeRestriction> m_allUnknownDatatypeRestrictions;
    protected final Set<String> m_definedDatatypeIRIs;
    protected final Set<Individual> m_allIndividuals;
    protected final Set<DescriptionGraph> m_allDescriptionGraphs;
    protected final Map<AtomicRole,Map<Individual,Set<Constant>>> m_dataPropertyAssertions;

    /**
     * @param ontologyIRI ontologyIRI
     * @param dlClauses dlClauses
     * @param positiveFacts positiveFacts
     * @param negativeFacts negativeFacts
     * @param atomicConcepts atomicConcepts
     * @param atomicObjectRoles atomicObjectRoles
     * @param allComplexObjectRoles allComplexObjectRoles
     * @param atomicDataRoles atomicDataRoles
     * @param allUnknownDatatypeRestrictions allUnknownDatatypeRestrictions
     * @param definedDatatypeIRIs definedDatatypeIRIs
     * @param individuals individuals
     * @param hasInverseRoles hasInverseRoles
     * @param hasAtMostRestrictions hasAtMostRestrictions
     * @param hasNominals hasNominals
     * @param hasDatatypes hasDatatypes
     */
    public DLOntology(String ontologyIRI,Collection<DLClause> dlClauses,Set<Atom> positiveFacts,Set<Atom> negativeFacts, Set<AtomicConcept> atomicConcepts,
            Set<AtomicRole> atomicObjectRoles,Set<Role> allComplexObjectRoles,Set<AtomicRole> atomicDataRoles,
            Set<DatatypeRestriction> allUnknownDatatypeRestrictions,Set<String> definedDatatypeIRIs,Set<Individual> individuals,
            boolean hasInverseRoles,boolean hasAtMostRestrictions,boolean hasNominals,boolean hasDatatypes) {
        m_ontologyIRI=ontologyIRI;
        m_dlClauses=dlClauses;
        m_positiveFacts=positiveFacts;
        m_negativeFacts=negativeFacts;
        m_hasInverseRoles=hasInverseRoles;
        m_hasAtMostRestrictions=hasAtMostRestrictions;
        m_hasNominals=hasNominals;
        m_hasDatatypes=hasDatatypes;
        if (atomicConcepts==null) {
            m_allAtomicConcepts=new TreeSet<>(Comparator.comparing(AtomicConcept::getIRI));
        } else {
            m_allAtomicConcepts=Collections.newSetFromMap(new IdentityHashMap<>());
            m_allAtomicConcepts.addAll(atomicConcepts);
        }
        int numberOfExternalConcepts=0;
        for (AtomicConcept c : m_allAtomicConcepts)
            if (!Prefixes.isInternalIRI(c.getIRI()))
                numberOfExternalConcepts++;
        m_numberOfExternalConcepts=numberOfExternalConcepts;
        if (atomicObjectRoles==null)
            m_allAtomicObjectRoles=new TreeSet<>(Comparator.comparing(AtomicRole::getIRI));
        else
            m_allAtomicObjectRoles=atomicObjectRoles;
        if (allComplexObjectRoles==null)
            m_allComplexObjectRoles=new HashSet<>();
        else
            m_allComplexObjectRoles=allComplexObjectRoles;
        if (atomicDataRoles==null)
            m_allAtomicDataRoles=new TreeSet<>(Comparator.comparing(AtomicRole::getIRI));
        else
            m_allAtomicDataRoles=atomicDataRoles;
        if (allUnknownDatatypeRestrictions==null)
            m_allUnknownDatatypeRestrictions=new HashSet<>();
        else
            m_allUnknownDatatypeRestrictions=allUnknownDatatypeRestrictions;
        if (definedDatatypeIRIs==null)
            m_definedDatatypeIRIs=new HashSet<>();
        else
            m_definedDatatypeIRIs=definedDatatypeIRIs;
        if (individuals==null)
            m_allIndividuals=new TreeSet<>(Comparator.comparing(Individual::getIRI));
        else
            m_allIndividuals=individuals;
        m_allDescriptionGraphs=new HashSet<>();
        boolean isHorn=true;
        for (DLClause dlClause : m_dlClauses) {
            if (dlClause.getHeadLength()>1)
                isHorn=false;
            for (int bodyIndex=dlClause.getBodyLength()-1;bodyIndex>=0;--bodyIndex) {
                DLPredicate dlPredicate=dlClause.getBodyAtom(bodyIndex).getDLPredicate();
                addDLPredicate(dlPredicate);
            }
            for (int headIndex=dlClause.getHeadLength()-1;headIndex>=0;--headIndex) {
                DLPredicate dlPredicate=dlClause.getHeadAtom(headIndex).getDLPredicate();
                addDLPredicate(dlPredicate);
            }
        }
        m_isHorn=isHorn;
        m_dataPropertyAssertions=new HashMap<>();
        for (Atom atom : m_positiveFacts) {
            addDLPredicate(atom.getDLPredicate());
            for (int i=0;i<atom.getArity();++i) {
                Term argument=atom.getArgument(i);
                if (argument instanceof Individual)
                    m_allIndividuals.add((Individual)argument);
            }
            if (atom.getArity()==2) {
                Object possibleConstant=atom.getArgument(1);
                if (possibleConstant instanceof Constant) {
                    // We have a data role assertion, so we store it into the approrpiate arrays
                    Individual sourceIndividual=(Individual)atom.getArgument(0);
                    assert atom.getDLPredicate() instanceof AtomicRole;
                    AtomicRole atomicRole=(AtomicRole)atom.getDLPredicate();
                    Map<Individual,Set<Constant>> individualsToConstants;
                    if (m_dataPropertyAssertions.containsKey(atomicRole))
                        individualsToConstants=m_dataPropertyAssertions.get(atomicRole);
                    else {
                        individualsToConstants=new HashMap<>();
                        m_dataPropertyAssertions.put(atomicRole,individualsToConstants);
                    }
                    Set<Constant> constants;
                    if (individualsToConstants.containsKey(sourceIndividual))
                        constants=individualsToConstants.get(sourceIndividual);
                    else {
                        constants=new HashSet<>();
                        individualsToConstants.put(sourceIndividual,constants);
                    }
                    constants.add((Constant)possibleConstant);
                }
            }
        }
        for (Atom atom : m_negativeFacts) {
            addDLPredicate(atom.getDLPredicate());
            for (int i=0;i<atom.getArity();++i) {
                Term argument=atom.getArgument(i);
                if (argument instanceof Individual)
                    m_allIndividuals.add((Individual)argument);
            }
        }
    }
    protected void addDLPredicate(DLPredicate dlPredicate) {
        if (dlPredicate instanceof AtomicConcept)
            m_allAtomicConcepts.add((AtomicConcept)dlPredicate);
        else if (dlPredicate instanceof AtLeastConcept) {
            LiteralConcept literalConcept=((AtLeastConcept)dlPredicate).getToConcept();
            if (literalConcept instanceof AtomicConcept)
                m_allAtomicConcepts.add((AtomicConcept)literalConcept);
        }
        else if (dlPredicate instanceof DescriptionGraph)
            m_allDescriptionGraphs.add((DescriptionGraph)dlPredicate);
        else if (dlPredicate instanceof ExistsDescriptionGraph)
            m_allDescriptionGraphs.add(((ExistsDescriptionGraph)dlPredicate).getDescriptionGraph());
    }

    /**
     * @return ontology iri
     */
    public String getOntologyIRI() {
        return m_ontologyIRI;
    }
    /**
     * @return all atomic concepts
     */
    public Set<AtomicConcept> getAllAtomicConcepts() {
        return m_allAtomicConcepts;
    }
    /**
     * @param concept concept
     * @return true if contains atomic concept
     */
    public boolean containsAtomicConcept(AtomicConcept concept) {
        return m_allAtomicConcepts.contains(concept);
    }
    /**
     * @return umber of external concepts
     */
    public int getNumberOfExternalConcepts() {
        return m_numberOfExternalConcepts;
    }
    /**
     * @return all atomic object roles
     */
    public Set<AtomicRole> getAllAtomicObjectRoles() {
        return m_allAtomicObjectRoles;
    }
    /**
     * @param role role
     * @return true if object role contained
     */
    public boolean containsObjectRole(AtomicRole role) {
        return m_allAtomicObjectRoles.contains(role);
    }
    /**
     * @return all complex object roles
     */
    public Set<Role> getAllComplexObjectRoles() {
        return m_allComplexObjectRoles;
    }
    /**
     * @param role role
     * @return true if complex
     */
    public boolean isComplexObjectRole(Role role) {
        return m_allComplexObjectRoles.contains(role);
    }
    /**
     * @return all atomic data roles
     */
    public Set<AtomicRole> getAllAtomicDataRoles() {
        return m_allAtomicDataRoles;
    }
    /**
     * @param role role
     * @return true if contains data role
     */
    public boolean containsDataRole(AtomicRole role) {
        return m_allAtomicDataRoles.contains(role);
    }
    /**
     * @return all unknown data restrictions
     */
    public Set<DatatypeRestriction> getAllUnknownDatatypeRestrictions() {
        return m_allUnknownDatatypeRestrictions;
    }
    /**
     * @return all individuals
     */
    public Set<Individual> getAllIndividuals() {
        return m_allIndividuals;
    }
    /**
     * @param individual individual
     * @return true if contains individual
     */
    public boolean containsIndividual(Individual individual) {
        return m_allIndividuals.contains(individual);
    }
    /**
     * @return description graphs
     */
    public Set<DescriptionGraph> getAllDescriptionGraphs() {
        return m_allDescriptionGraphs;
    }
    /**
     * @return dl clauses
     */
    public Collection<DLClause> getDLClauses() {
        return m_dlClauses;
    }
    /**
     * @return positive facts
     */
    public Set<Atom> getPositiveFacts() {
        return m_positiveFacts;
    }
    /**
     * @return data assertions
     */
    public Map<AtomicRole,Map<Individual,Set<Constant>>> getDataPropertyAssertions() {
        return m_dataPropertyAssertions;
    }
    /**
     * @return negative facts
     */
    public Set<Atom> getNegativeFacts() {
        return m_negativeFacts;
    }
    /**
     * @return true if inverse roles
     */
    public boolean hasInverseRoles() {
        return m_hasInverseRoles;
    }
    /**
     * @return true if at most restrictions
     */
    public boolean hasAtMostRestrictions() {
        return m_hasAtMostRestrictions;
    }
    /**
     * @return true if has nominals
     */
    public boolean hasNominals() {
        return m_hasNominals;
    }
    /**
     * @return true if has datatypes
     */
    public boolean hasDatatypes() {
        return m_hasDatatypes;
    }
    /**
     * @return true if has unknown datatypes restrictions
     */
    public boolean hasUnknownDatatypeRestrictions() {
        return !m_allUnknownDatatypeRestrictions.isEmpty();
    }
    /**
     * @return true if Horn ontology
     */
    public boolean isHorn() {
        return m_isHorn;
    }
    /**
     * @return datatypes
     */
    public Set<String> getDefinedDatatypeIRIs() {
        return m_definedDatatypeIRIs;
    }
    protected Set<AtomicConcept> getBodyOnlyAtomicConcepts() {
        Set<AtomicConcept> bodyOnlyAtomicConcepts=new HashSet<>(m_allAtomicConcepts);
        for (DLClause dlClause : m_dlClauses)
            for (int headIndex=0;headIndex<dlClause.getHeadLength();headIndex++) {
                DLPredicate dlPredicate=dlClause.getHeadAtom(headIndex).getDLPredicate();
                bodyOnlyAtomicConcepts.remove(dlPredicate);
                if (dlPredicate instanceof AtLeastConcept)
                    bodyOnlyAtomicConcepts.remove(((AtLeastConcept)dlPredicate).getToConcept());
            }
        return bodyOnlyAtomicConcepts;
    }
    protected Set<AtomicRole> computeGraphAtomicRoles() {
        Set<AtomicRole> graphAtomicRoles=new HashSet<>();
        for (DescriptionGraph descriptionGraph : m_allDescriptionGraphs)
            for (int edgeIndex=0;edgeIndex<descriptionGraph.getNumberOfEdges();edgeIndex++) {
                DescriptionGraph.Edge edge=descriptionGraph.getEdge(edgeIndex);
                graphAtomicRoles.add(edge.getAtomicRole());
            }
        boolean change=true;
        while (change) {
            change=false;
            for (DLClause dlClause : m_dlClauses)
                if (containsAtomicRoles(dlClause,graphAtomicRoles))
                    if (addAtomicRoles(dlClause,graphAtomicRoles))
                        change=true;
        }
        return graphAtomicRoles;
    }
    protected boolean containsAtomicRoles(DLClause dlClause,Set<AtomicRole> roles) {
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole && roles.contains(dlPredicate))
                return true;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole && roles.contains(dlPredicate))
                return true;
        }
        return false;
    }
    protected boolean addAtomicRoles(DLClause dlClause,Set<AtomicRole> roles) {
        boolean change=false;
        for (int atomIndex=0;atomIndex<dlClause.getBodyLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getBodyAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole)
                if (roles.add((AtomicRole)dlPredicate))
                    change=true;
        }
        for (int atomIndex=0;atomIndex<dlClause.getHeadLength();atomIndex++) {
            DLPredicate dlPredicate=dlClause.getHeadAtom(atomIndex).getDLPredicate();
            if (dlPredicate instanceof AtomicRole)
                if (roles.add((AtomicRole)dlPredicate))
                    change=true;
        }
        return change;
    }
    /**
     * @param prefixes prefixes
     * @return toString
     */
    public String toString(Prefixes prefixes) {
        StringBuilder builder=new StringBuilder("Prefixes: [").append(CRLF);
        for (Map.Entry<String,String> entry : prefixes.getPrefixIRIsByPrefixName().entrySet()) {
            builder.append("  ").append(entry.getKey()).append(" = <").append(entry.getValue()).append('>').append(CRLF);
        }
        builder.append("]").append(CRLF).append("Deterministic DL-clauses: [").append(CRLF);
        int numDeterministicClauses=0;
        for (DLClause dlClause : m_dlClauses)
            if (dlClause.getHeadLength()<=1) {
                numDeterministicClauses++;
                builder.append("  ").append(dlClause.toString(prefixes)).append(CRLF);
            }
        builder.append("]").append(CRLF).append("Disjunctive DL-clauses: [").append(CRLF);
        int numNondeterministicClauses=0;
        int numDisjunctions=0;
        for (DLClause dlClause : m_dlClauses)
            if (dlClause.getHeadLength()>1) {
                numNondeterministicClauses++;
                numDisjunctions+=dlClause.getHeadLength();
                builder.append("  ").append(dlClause.toString(prefixes)).append(CRLF);
            }
        builder.append("]").append(CRLF).append("ABox: [").append(CRLF);
        for (Atom atom : m_positiveFacts) {
            builder.append("  ").append(atom.toString(prefixes)).append(CRLF);
        }
        for (Atom atom : m_negativeFacts) {
            builder.append("  !").append(atom.toString(prefixes)).append(CRLF);
        }
        builder.append("]").append(CRLF).append("Statistics: [").append(CRLF)
        .append("  Number of deterministic clauses: " + numDeterministicClauses).append(CRLF)
        .append("  Number of nondeterministic clauses: " + numNondeterministicClauses).append(CRLF)
        .append("  Number of disjunctions: " + numDisjunctions).append(CRLF)
        .append("  Number of positive facts: " + m_positiveFacts.size()).append(CRLF)
        .append("  Number of negative facts: " + m_negativeFacts.size()).append(CRLF).append("]");
        return builder.toString();
    }
    /**
     * @return statistics
     */
    public String getStatistics() {
        return getStatistics(null,null,null);
    }
    protected String getStatistics(Integer deterministicClauses, Integer nondeterministicClauses, Integer disjunctions) {
        int numDeterministicClauses;int numNondeterministicClauses; int numDisjunctions;
        if (deterministicClauses==null || nondeterministicClauses==null || disjunctions==null) {
            numDeterministicClauses=0;
            numNondeterministicClauses=0;
            numDisjunctions=0;
            for (DLClause dlClause : m_dlClauses) {
                if (dlClause.getHeadLength()<=1)
                    numDeterministicClauses++;
                else {
                    numNondeterministicClauses++;
                    numDisjunctions+=dlClause.getHeadLength();
                }
            }
        }else {
            numDeterministicClauses=deterministicClauses.intValue();
            numNondeterministicClauses=nondeterministicClauses.intValue();
            numDisjunctions=disjunctions.intValue();
        }
        StringBuilder builder=new StringBuilder("DL clauses statistics: [").append(CRLF)
                .append("  Number of deterministic clauses: " ).append( numDeterministicClauses).append(CRLF)
                .append("  Number of nondeterministic clauses: " ).append( numNondeterministicClauses).append(CRLF)
                .append("  Overall number of disjunctions: " ).append( numDisjunctions).append(CRLF)
                .append("  Number of positive facts: " ).append( m_positiveFacts.size()).append(CRLF)
                .append("  Number of negative facts: " ).append( m_negativeFacts.size()).append(CRLF)
                .append("  Inverses: " ).append( this.hasInverseRoles()).append(CRLF)
                .append("  At-Mosts: " ).append( this.hasAtMostRestrictions()).append(CRLF)
                .append("  Datatypes: " ).append( this.hasDatatypes()).append(CRLF)
                .append("  Nominals: " ).append( this.hasNominals()).append(CRLF)
                .append("  Number of atomic concepts: " ).append( m_allAtomicConcepts.size()).append(CRLF)
                .append("  Number of object properties: ").append( m_allAtomicObjectRoles.size()).append(CRLF)
                .append("  Number of data properties: ").append( m_allAtomicDataRoles.size()).append(CRLF)
                .append("  Number of individuals: " ).append( m_allIndividuals.size()).append(CRLF).append("]");
        return builder.toString();
    }
    @Override
    public String toString() {
        return toString(Prefixes.STANDARD_PREFIXES);
    }
    /**
     * @param outputStream outputStream
     * @throws IOException if reading fails
     */
    public void save(OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.flush();
    }
    /**
     * @param inputStream inputStream
     * @return ontology
     * @throws IOException if class not found
     */
    public static DLOntology load(InputStream inputStream) throws IOException {
        try {
            ObjectInputStream objectInputStream=new ObjectInputStream(inputStream);
            return (DLOntology)objectInputStream.readObject();
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
