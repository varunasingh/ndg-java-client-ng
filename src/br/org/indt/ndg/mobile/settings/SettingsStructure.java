package br.org.indt.ndg.mobile.settings;

import br.org.indt.ndg.lwuit.extended.DateField;
import br.org.indt.ndg.mobile.AppMIDlet;
import br.org.indt.ndg.mobile.NdgConsts;
import java.io.PrintStream;
import br.org.indt.ndg.mobile.settings.PhotoSettings.PhotoResolution;
import br.org.indt.ndg.mobile.structures.Language;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * READ FIRST!
 * To add a new setting You need to perform 5 steps:
 * 1) Implement Setter and Getter for new setting
 * 2) Add default setting value constant
 * 3) Apply the constant to initial value of new setting
 * 4) Add setting default value to createDefaultSettings method
 * 5) Update SettingsHandler
 * @author tomasz.baniak
 */

public class SettingsStructure {

    public static final int NOT_REGISTERED = 0;
    public static final int REGISTERED = 1;

    /* Default values */
    private static final int DEFAULT_SPLASH_TIME = 8000;
    private static final int DEFAULT_IS_REGISTERED = NOT_REGISTERED;
    private static final boolean DEFAULT_GPS = true;
    private static final boolean DEFAULT_GEO_TAGGING = true;
    private static final int DEFAULT_PHOTO_RESULUTION_ID = 0;
    private static final int DEFAULT_STYLE_ID = 0;
    private static final boolean DEFAULT_LOG_SUPPORT = false;
    private static final int DEFAULT_DATE_FORMAT_ID = DateField.DDMMYYYY;
    private static final boolean DEFAULT_ENCRYPTION = false;
    private static final int DEFAULT_ENCRIPTION_CONFIGURED = 0;
    private static final String DEFAULT_LANGUAGE_NAME = "Default (English)";
    private static final String DEFAULT_LANGUAGE_LOCALE = "en-GB";
    private String server_url = null;
    private int splash_time = DEFAULT_SPLASH_TIME;
    private int isRegistered_flag = DEFAULT_IS_REGISTERED;
    private boolean gps_configured = DEFAULT_GPS;
    private boolean geoTagging_configured = DEFAULT_GEO_TAGGING;
    private int selectedResolution = DEFAULT_PHOTO_RESULUTION_ID;
    private int selectedStyle = DEFAULT_STYLE_ID;
    private boolean logSupport = DEFAULT_LOG_SUPPORT;
    private int dateFormatId = DEFAULT_DATE_FORMAT_ID;
    private int encryptionConfigured = DEFAULT_ENCRIPTION_CONFIGURED;
    private boolean encryption = DEFAULT_ENCRYPTION;
    private String language = DEFAULT_LANGUAGE_NAME;
    private String appVersion;
    private Language defaultLanguage = new Language(DEFAULT_LANGUAGE_NAME, DEFAULT_LANGUAGE_LOCALE);
    private Vector languages = new Vector();

    public SettingsStructure() {
        initializeDefaultRuntimeSettings();
    }

    private void initializeDefaultRuntimeSettings() {
        appVersion = AppMIDlet.getInstance().getAppVersion();
        languages.addElement(defaultLanguage);
    }

    public void createDefaultSettings(PrintStream _out) throws UnsupportedEncodingException {
        // Reset to default values
        setLanguage(defaultLanguage.getLocale());
        setRegisteredFlag(DEFAULT_IS_REGISTERED);
        setSplashTime(DEFAULT_SPLASH_TIME);
        setGpsConfigured(DEFAULT_GPS);
        setGeoTaggingConfigured(DEFAULT_GEO_TAGGING);
        setPhotoResolutionId(DEFAULT_PHOTO_RESULUTION_ID);
        setStyleId(DEFAULT_STYLE_ID);
        setLogSupport(DEFAULT_LOG_SUPPORT);
        setDateFormatId(DEFAULT_DATE_FORMAT_ID);
        setEncryptionConfigured(DEFAULT_ENCRIPTION_CONFIGURED);
        setEncryption(DEFAULT_ENCRYPTION);
        setAppVersion(AppMIDlet.getInstance().getAppVersion());
        languages.removeAllElements();
        languages.addElement(defaultLanguage);

        saveSettings(_out);
    }

    public void saveSettings(PrintStream _out) throws UnsupportedEncodingException {
        _out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        _out.print("<settings");
        _out.print(" registered=\"" + getRegisteredFlag() + "\"");
        _out.print(" splash=\"" + getSplashTime() + "\"");
        _out.print(" language=\"" + getLanguage() + "\"");
        _out.println(" showEncryptionScreen=\""+ (isEncryptionConfigured() ? "1" : "0") + "\">");

        writeGpsSettings(_out);
        writeGeoTaggingSettings(_out);
        writePhotoResolutionSettings(_out);
        writeStyleSettings(_out);
        writeLogSettings(_out);
        writeServerSettings(_out);
        writeLanguageSettings(_out);
        writeVersionSettings(_out);
        writeDateFormatSettings(_out);
        writeEncryption(_out);

        _out.println("</settings>");
    }

    public void writeServerSettings(PrintStream _out) {
        _out.println("<server>");
        if(server_url != null)
        {
            _out.println("<url_server>" + server_url + "</url_server>");
        }
        _out.println("</server>");
    }

    private void writeLanguageSettings(PrintStream _out) throws UnsupportedEncodingException {

        if(languages != null)
        {
            _out.println("<languages>");
            StringBuffer languageString = null;
            for(int i = 0 ; i < languages.size(); i++)
            {
                languageString = new StringBuffer();
                String languageInLatin1 = new String(((Language)languages.elementAt(i)).getLangName().getBytes("UTF-8"), "ISO-8859-1");
                languageString.append("<language name=\"").append(languageInLatin1);
                languageString.append("\" locale= \"").append(((Language)languages.elementAt(i)).getLocale()).append("\"/>");
                _out.println(languageString);
            }
            _out.println("</languages>");
        }

    }

    public void writeGpsSettings(PrintStream _out) {
        _out.print("<gps configured=\"");
        if (gps_configured) {
            _out.println("yes\"/>");
        } else {
            _out.println("no\"/>");
        }
    }

    public void writeGeoTaggingSettings(PrintStream _out) {
        _out.print("<geotagging configured=\"");
        if (geoTagging_configured) {
            _out.println("yes\"/>");
        } else {
            _out.println("no\"/>");
        }
    }

    void writeLogSettings(PrintStream _out) {
        String strLogSupport = logSupport ? "yes" : "no";
        _out.println("<log active=\"" + strLogSupport + "\"" + "/>");
    }

    void writeVersionSettings(PrintStream _out) {
        _out.println("<version application=\"" + appVersion + "\"/>");
    }

    void writePhotoResolutionSettings(PrintStream output) {
        output.print("<photoResolution configId=\"");
        output.print( String.valueOf(selectedResolution) );
        output.println( "\"/>" );
    }

    void writeStyleSettings(PrintStream output) {
        output.print("<style id=\"");
        output.print( String.valueOf(selectedStyle) );
        output.println( "\"/>" );
    }

    void writeDateFormatSettings(PrintStream output){
        output.print("<dateFormat id=\"");
        output.print( String.valueOf(dateFormatId) );
        output.println( "\"/>" );
    }

    public void writeEncryption(PrintStream _out) {
        _out.print("<encryption enabled=\"");
        if (encryption) {
            _out.println("yes\"/>");
        } else {
            _out.println("no\"/>");
        }
    }

    void setLogSupport(boolean _logSupport) {
        logSupport = _logSupport;
    }
    public boolean getLogSupport(){
        return logSupport;
    }

    public void setLanguage(String _lang) {
        language = _lang;
    }
    public String getLanguage() {
        if(language == null || language.equals("")){
            language = defaultLanguage.getLocale();
        }
        return language;
    }

    void setAppVersion(String _ver) {
        appVersion = _ver;
    }
    public String getAppVersion() {
        return appVersion;
    }

    public String getUpdateCheckURL() {
        return new StringBuffer(server_url).append(NdgConsts.SERVLET_CLIENT_UPDATE).toString();
    }

    public void setServerUrl(String _url) {
        if(_url.endsWith("/"))
        {
            server_url = _url;
        } else {
            server_url = _url + "/";
        }
    }

    public String getDateFormatString(){
        //TODO format to string;
       return "0";
    }

    public int getDateFormatId(){
        return dateFormatId;
    }

    public void setDateFormatId(int _id){
        dateFormatId = _id;
    }

    public String getServerUrl() {
        return server_url;
    }

    public String getServerLocalDirName() {
        return server_url.replace( '.', '_' ).replace( ':', '_' ).replace( '/', '_' ).replace('\\','_');
    }

    public String getPostResultsUrl() {
        return new StringBuffer(server_url).append(NdgConsts.SERVLET_POST_RESULTS).toString();
    }

    public String getTestAuthorizationUrl() {
        return new StringBuffer(server_url).append(NdgConsts.SERVLET_CHECK_AUTHORIZATION).toString();
    }

    public String getReceiveSurveyURL(){
        return new StringBuffer(server_url).append(NdgConsts.SERVLET_RECEIVE_SURVEY).toString();
    }

    public String getLocalizationServingTextURL() {
        return new StringBuffer(server_url).append(NdgConsts.SERVLET_LOCALIZATION).
                        append('/').append(NdgConsts.SERVLET_LANGUAGE_TEXT).toString();
    }

    public String getLocalizationServingFontURL() {
        return new StringBuffer(server_url).append(NdgConsts.SERVLET_LOCALIZATION).
                        append('/').append(NdgConsts.SERVLET_LANGUAGE_FONT).toString();
    }

    public String getTestConnectionUrl() {
        return new StringBuffer(server_url).append(NdgConsts.SERVLET_TEST_CONNECTION).toString();
    }

    public String getLanguageListURL() {
        return new StringBuffer(server_url).append(NdgConsts.SERVLET_LANGUAGE_LIST).toString();
    }

    public void setRegisteredFlag(int _flag) {
        isRegistered_flag = _flag;
    }

    public int getRegisteredFlag() {
        return isRegistered_flag;
    }

    public void setSplashTime(int _time) {
        splash_time = _time;
    }

    public int getSplashTime() {
        return splash_time;
    }

    public void setGpsConfigured(boolean _state) {
        gps_configured = _state;
    }
    public boolean getGpsConfigured() {
        return gps_configured;
    }

    public void setGeoTaggingConfigured(boolean _state) {
        geoTagging_configured = _state;
    }
    public boolean getGeoTaggingConfigured() {
        return geoTagging_configured;
    }

    public void setPhotoResolutionId(int _resConf ) {
        selectedResolution = _resConf;
    }
    public int getPhotoResolutionId() {
        return selectedResolution;
    }

    public void setStyleId(int styleId ) {
        selectedStyle = styleId;
    }
    public int getStyleId() {
        return selectedStyle;
    }

    public PhotoResolution getPhotoResolution() {
        return PhotoSettings.getInstance().getPhotoResolution( selectedResolution );
    }

    public String[] getResolutionList() {
        return PhotoSettings.getInstance().getResolutionList();
    }

    public boolean isEncryptionConfigured() {
        return encryptionConfigured== 1 ? true : false;
    }

    public void setEncryptionConfigured(int encryption) {
        encryptionConfigured = encryption;
    }

    public boolean getEncryption() {
        return encryption;
    }

    public void setEncryption(boolean encrypt) {
        encryption = encrypt;
    }


    public Vector getLanguages() {
        return languages;
    }

    public void setLanguages(Vector languages) {
        this.languages = languages;
    }

    public Language getDefaultLanguage(){
        return defaultLanguage;
    }
}
