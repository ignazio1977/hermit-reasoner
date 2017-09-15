package org.semanticweb.HermiT.datatypes.xmlliteral;

import org.apache.axiom.c14n.impl.Canonicalizer20010315ExclWithComments;


/**
 * XML literal.
 */
public class XMLLiteral {
    protected final String m_xml;

    /**
     * @param xml xml
     */
    public XMLLiteral(String xml) {
        m_xml=xml;
    }
    /**
     * @return xml
     */
    public String getXML() {
        return m_xml;
    }
    @Override
    public boolean equals(Object that) {
        if (this==that)
            return true;
        if (!(that instanceof XMLLiteral))
            return false;
        return ((XMLLiteral)that).m_xml.equals(m_xml);
    }
    @Override
    public int hashCode() {
        return m_xml.hashCode();
    }
    @Override
    public String toString() {
        return m_xml;
    }
    /**
     * @param lexicalForm string to parse
     * @return parsed literal
     * @throws Exception if a canonicalization exception happens
     */
    public static XMLLiteral parse(String lexicalForm) throws Exception {
        String enclosedXML="<arbitraryTag>"+lexicalForm+"</arbitraryTag>";
        Canonicalizer20010315ExclWithComments canonicalizer=new Canonicalizer20010315ExclWithComments();
        byte result[]=canonicalizer.engineCanonicalize(enclosedXML.getBytes("UTF-8"));
        String canonicalXML=new String(result,"UTF-8");
        assert canonicalXML.startsWith("<arbitraryTag>");
        assert canonicalXML.endsWith("</arbitraryTag>");
        canonicalXML=canonicalXML.substring("<arbitraryTag>".length(),canonicalXML.length()-"</arbitraryTag>".length());
        return new XMLLiteral(canonicalXML);
    }
}
