package org.exist.library.mail;

import org.exist.dom.QName;
import org.exist.xquery.*;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;

import java.util.List;
import java.util.Map;

import static org.exist.xquery.FunctionDSL.functionDefs;

/**
 * A very simple example XQuery Library Module implemented
 * in Java.
 */
public class MailModule extends AbstractInternalModule {

    public static final String NAMESPACE_URI = "https://exist-db.org/xquery/mail-new";
    public static final String PREFIX = "mail";
    public static final String RELEASED_IN_VERSION = "eXist-5.3.1";

    // register the functions of the module
    public static final FunctionDef[] functions = functionDefs(
        functionDefs(MailFunctions.class,
                MailFunctions.FS_HELLO_WORLD,
                MailFunctions.FS_SAY_HELLO,
                MailFunctions.FS_ADD
        )
    );

    public MailModule(final Map<String, List<? extends Object>> parameters) {
        super(functions, parameters);
    }

    @Override
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }

    @Override
    public String getDefaultPrefix() {
        return PREFIX;
    }

    @Override
    public String getDescription() {
        return "Mail Module for eXist-db XQuery";
    }

    @Override
    public String getReleaseVersion() {
        return RELEASED_IN_VERSION;
    }

    static FunctionSignature functionSignature(final String name, final String description,
            final FunctionReturnSequenceType returnType, final FunctionParameterSequenceType... paramTypes) {
        return FunctionDSL.functionSignature(new QName(name, NAMESPACE_URI), description, returnType, paramTypes);
    }

    static FunctionSignature[] functionSignatures(final String name, final String description,
            final FunctionReturnSequenceType returnType, final FunctionParameterSequenceType[][] variableParamTypes) {
        return FunctionDSL.functionSignatures(new QName(name, NAMESPACE_URI), description, returnType, variableParamTypes);
    }

    static class ExpathBinModuleErrorCode extends ErrorCodes.ErrorCode {
        private ExpathBinModuleErrorCode(final String code, final String description) {
            super(new QName(code, NAMESPACE_URI, PREFIX), description);
        }
    }
}
