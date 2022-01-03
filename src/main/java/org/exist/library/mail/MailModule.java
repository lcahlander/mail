package org.exist.library.mail;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.exist.dom.QName;
import org.exist.xquery.*;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.modules.ModuleUtils;
import org.exist.xquery.modules.ModuleUtils.ContextMapEntryModifier;
import org.exist.xquery.modules.ModuleUtils.ContextMapModifierWithoutResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A very simple example XQuery Library Module implemented
 * in Java.
 */
public class MailModule extends AbstractInternalModule {

    private final static Logger LOG = LogManager.getLogger( MailModule.class );

    public static final String NAMESPACE_URI = "https://exist-db.org/xquery/mail-new";
    public static final String PREFIX = "mail";
    public static final String RELEASED_IN_VERSION = "eXist-5.3.1";

    // register the functions of the module
    public static final FunctionDef[] functions = {
            new FunctionDef(MailSessionFunctions.signatures[0], MailSessionFunctions.class),
            new FunctionDef(MailStoreFunctions.signatures[0], MailStoreFunctions.class),
            new FunctionDef(MailStoreFunctions.signatures[1], MailStoreFunctions.class),
            new FunctionDef(MailFolderFunctions.signatures[0], MailFolderFunctions.class),
            new FunctionDef(MailFolderFunctions.signatures[1], MailFolderFunctions.class),
            new FunctionDef(MessageListFunctions.signatures[0], MessageListFunctions.class),
            new FunctionDef(MessageListFunctions.signatures[1], MessageListFunctions.class),
            new FunctionDef(MessageListFunctions.signatures[2], MessageListFunctions.class),
            new FunctionDef(MessageListFunctions.signatures[3], MessageListFunctions.class),
            new FunctionDef(MessageFunctions.signatures[0], MessageFunctions.class),
            new FunctionDef(SendEmailFunction.signatures[0], SendEmailFunction.class),

            // deprecated functions:
            new FunctionDef(SendEmailFunction.deprecated, SendEmailFunction.class)
    };

    public final static String SESSIONS_CONTEXTVAR = "_eXist_mail_sessions";
    public final static String STORES_CONTEXTVAR = "_eXist_mail_stores";
    public final static String FOLDERS_CONTEXTVAR = "_eXist_mail_folders";
    public final static String FOLDERMSGLISTS_CONTEXTVAR = "_eXist_folder_message_lists";
    public final static String MSGLISTS_CONTEXTVAR = "_eXist_mail_message_lists";

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

    //***************************************************************************
    //*
    //*    Session Methods
    //*
    //***************************************************************************/

    /**
     * Retrieves a previously stored Session from the Context of an XQuery
     *
     * @param context 			The Context of the XQuery containing the Session
     * @param sessionHandle	 	The handle of the Session to retrieve from the Context of the XQuery
     */
    static Session retrieveSession(XQueryContext context, long sessionHandle) {
        return ModuleUtils.retrieveObjectFromContextMap(context, MailModule.SESSIONS_CONTEXTVAR, sessionHandle);
    }

    /**
     * Stores a Session in the Context of an XQuery
     *
     * @param context 	The Context of the XQuery to store the Session in
     * @param session 	The Session to store
     *
     * @return A unique handle representing the Session
     */
    static long storeSession(XQueryContext context, Session session) {
        return ModuleUtils.storeObjectInContextMap(context, MailModule.SESSIONS_CONTEXTVAR, session);
    }

    //***************************************************************************
    //*
    //*    Store Methods
    //*
    //***************************************************************************/

    /**
     * Retrieves a previously saved Store from the Context of an XQuery
     *
     * @param context 			The Context of the XQuery containing the Store
     * @param storeHandle	 	The handle of the Store to retrieve from the Context of the XQuery
     */
    static Store retrieveStore(XQueryContext context, long storeHandle) {
        return ModuleUtils.retrieveObjectFromContextMap(context, MailModule.STORES_CONTEXTVAR, storeHandle);
    }

    /**
     * Saves a Store in the Context of an XQuery
     *
     * @param context 	The Context of the XQuery to save the Store in
     * @param store 	The Store to store
     *
     * @return A unique handle representing the Store
     */
    static long storeStore(XQueryContext context, Store store) {
        return ModuleUtils.storeObjectInContextMap(context, MailModule.STORES_CONTEXTVAR, store);
    }

    /**
     * Remove the store from the specified XQueryContext
     *
     * @param context The context to remove the store for
     */
    static void removeStore(XQueryContext context, final long storeHandle) {

        ModuleUtils.modifyContextMap(context, MailModule.STORES_CONTEXTVAR, (ContextMapModifierWithoutResult<Store>) map -> map.remove(storeHandle));

        //update the context
        //context.setXQueryContextVar(MailModule.STORES_CONTEXTVAR, stores);
    }

    /**
     * Closes all the open stores for the specified XQueryContext
     *
     * @param context The context to close stores for
     */
    private static void closeAllStores(XQueryContext context)  {
        ModuleUtils.modifyContextMap(context,  MailModule.STORES_CONTEXTVAR, new ContextMapEntryModifier<Store>(){
            @Override
            public void modifyWithoutResult(final Map<Long, Store> map) {
                super.modifyWithoutResult(map);

                //remove all stores from map
                map.clear();
            }

            @Override
            public void modifyEntry(final Map.Entry<Long, Store> entry) {
                final Store store = entry.getValue();
                try {
                    // close the store
                    store.close();
                }  catch(MessagingException me) {
                    LOG.warn("Unable to close Mail Store: {}", me.getMessage(), me);
                }
            }
        });

        // update the context
        //context.setXQueryContextVar(MailModule.STORES_CONTEXTVAR, stores);
    }

    //***************************************************************************
    //*
    //*    Folder Methods
    //*
    //***************************************************************************/

    /**
     * Retrieves a previously saved Folder from the Context of an XQuery
     *
     * @param context 			The Context of the XQuery containing the Folder
     * @param folderHandle	 	The handle of the Folder to retrieve from the Context of the XQuery
     */
    static Folder retrieveFolder(XQueryContext context, long folderHandle) {
        return ModuleUtils.retrieveObjectFromContextMap(context, MailModule.FOLDERS_CONTEXTVAR, folderHandle);
    }

    /**
     * Saves a Folder in the Context of an XQuery
     *
     * @param context 	The Context of the XQuery to save the Folder in
     * @param folder 	The Folder to store
     *
     * @return A unique handle representing the Store
     */
    static long storeFolder(XQueryContext context, Folder folder) {
        return ModuleUtils.storeObjectInContextMap(context,  MailModule.FOLDERS_CONTEXTVAR, folder);
    }

    /**
     * Remove the folder from the specified XQueryContext
     *
     * @param context The context to remove the store for
     */
    static void removeFolder(final XQueryContext context, final long folderHandle) {

        ModuleUtils.modifyContextMap(context, MailModule.FOLDERS_CONTEXTVAR, (ContextMapModifierWithoutResult<Folder>) map -> {

            //remove the message lists for the folder
            ModuleUtils.modifyContextMap(context, MailModule.FOLDERMSGLISTS_CONTEXTVAR, (ContextMapModifierWithoutResult<Map<Long, Message[]>>) map12 -> {

                final Map<Long, Message[]> folderMsgList = map12.get(folderHandle);

                ModuleUtils.modifyContextMap(context, MailModule.MSGLISTS_CONTEXTVAR, (ContextMapModifierWithoutResult<Message[]>) map1 -> folderMsgList.keySet().forEach(map1::remove));

                //remove the folder message kist
                map12.remove(folderHandle);
            });

            //remove the folder
            map.remove(folderHandle);
        });
    }


    /**
     * Closes all the open folders for the specified XQueryContext
     *
     * @param context The context to close folders for
     */
    private static void closeAllFolders(XQueryContext context) {
        ModuleUtils.modifyContextMap(context, MailModule.FOLDERS_CONTEXTVAR, new ContextMapEntryModifier<Folder>(){

            @Override
            public void modifyWithoutResult(final Map<Long, Folder> map) {
                super.modifyWithoutResult(map);

                //remove all from the folders map
                map.clear();
            }

            @Override
            public void modifyEntry(final Map.Entry<Long, Folder> entry) {
                final Folder folder = entry.getValue();

                //close the folder
                try {
                    folder.close(false);
                } catch(MessagingException me) {
                    LOG.warn("Unable to close Mail Folder: {}", me.getMessage(), me);
                }
            }
        });

        // update the context
        // context.setXQueryContextVar( MailModule.FOLDERS_CONTEXTVAR, folders );
    }


    //***************************************************************************
    //*
    //*    Message List Methods
    //*
    //***************************************************************************/

    /**
     * Retrieves a previously saved MessageList from the Context of an XQuery
     *
     * @param context 			The Context of the XQuery containing the Message List
     * @param msgListHandle	 	The handle of the Message List to retrieve from the Context of the XQuery
     */
    static Message[] retrieveMessageList(XQueryContext context, long msgListHandle) {
        return ModuleUtils.retrieveObjectFromContextMap(context, MailModule.MSGLISTS_CONTEXTVAR, msgListHandle);
    }


    /**
     * Saves a MessageList in the Context of an XQuery
     *
     * @param context 	The Context of the XQuery to save the MessageList in
     * @param msgList 	The MessageList to store
     *
     * @return A unique handle representing the Store
     */
    static long storeMessageList(XQueryContext context, final Message[] msgList, final long folderHandle) {

        final long msgListHandle = ModuleUtils.storeObjectInContextMap(context, MailModule.MSGLISTS_CONTEXTVAR, msgList);

        ModuleUtils.modifyContextMap(context, MailModule.FOLDERMSGLISTS_CONTEXTVAR, (ContextMapModifierWithoutResult<Map<Long, Message[]>>) map -> {
            Map<Long, Message[]> folderMsgList = map.computeIfAbsent(folderHandle, k -> new HashMap<>());

            folderMsgList.put(msgListHandle, msgList);
        });

        return msgListHandle;
    }


    /**
     * Remove the MessageList from the specified XQueryContext
     *
     * @param context The context to remove the MessageList for
     */
    static void removeMessageList(XQueryContext context, final long msgListHandle) {
        ModuleUtils.modifyContextMap(context, MailModule.MSGLISTS_CONTEXTVAR, (ContextMapModifierWithoutResult<Message[]>) map -> map.remove(msgListHandle));

        // update the context
        //context.setXQueryContextVar( MailModule.MSGLISTS_CONTEXTVAR, msgLists );
    }

    /**
     * Closes all the open MessageLists for the specified XQueryContext
     *
     * @param context The context to close MessageLists for
     */
    private static void closeAllMessageLists(XQueryContext context) {
        ModuleUtils.modifyContextMap(context, MailModule.MSGLISTS_CONTEXTVAR, (ContextMapModifierWithoutResult<Message[]>) Map::clear);

        // update the context
        //context.setXQueryContextVar( MailModule.MSGLISTS_CONTEXTVAR, msgLists );
    }


    /**
     * Resets the Module Context and closes any open mail stores/folders/message lists for the XQueryContext
     *
     * @param context The XQueryContext
     */
    @Override
    public void reset( XQueryContext context, boolean keepGlobals ) {
        // reset the module context
        super.reset(context, keepGlobals);

        // close any open MessageLists
        closeAllMessageLists(context);

        // close any open folders
        closeAllFolders(context);

        // close any open stores
        closeAllStores(context);
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
