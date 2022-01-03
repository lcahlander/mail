package org.exist.library.mail;

import java.util.Properties;

import javax.mail.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.dom.QName;
import org.exist.util.ParametersExtractor;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.IntegerValue;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;

public class MailSessionFunctions extends BasicFunction
{
    protected static final Logger logger = LogManager.getLogger(MailSessionFunctions.class);

    public final static FunctionSignature signatures[] = {
            new FunctionSignature(
                    new QName( "get-mail-session", MailModule.NAMESPACE_URI, MailModule.PREFIX ),
                    "Open's a JavaMail session.",
                    new SequenceType[]
                            {
                                    new FunctionParameterSequenceType( "properties", Type.ELEMENT, Cardinality.ZERO_OR_ONE, "An optional JavaMail session properties in the form <properties><property name=\"\" value=\"\"/></properties>.  The JavaMail properties are spelled out in Appendix A of the JavaMail specifications." )
                            },
                    new FunctionReturnSequenceType( Type.LONG, Cardinality.ZERO_OR_ONE, "an xs:long representing the session handle." )
            )
    };

    public MailSessionFunctions( XQueryContext context, FunctionSignature signature )
    {
        super( context, signature );
    }

    @Override
    public Sequence eval( Sequence[] args, Sequence contextSequence ) throws XPathException
    {
        Properties props = new Properties();

        if( args.length == 1 ) {
            // try and get the session properties
            props = ParametersExtractor.parseProperties( ((NodeValue) args[0].itemAt(0)).getNode() );
        }

        Session session = Session.getInstance( props, null );

        // store the session and return the handle of the session

        return new IntegerValue( MailModule.storeSession( context, session ) );
    }
}
