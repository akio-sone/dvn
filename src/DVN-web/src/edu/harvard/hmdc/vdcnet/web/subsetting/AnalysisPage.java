/*
 * Dataverse Network - A web application to distribute, share 
 * and analyze quantitative data.
 * Copyright (C) 2007
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation,Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

/*
 * AnalysisPage.java
 *
 * Created on October 12, 2006, 12:13 AM
 *
 */

package edu.harvard.hmdc.vdcnet.web.subsetting;

/**
 * 
 * @author asone
 */

import static java.lang.System.*;

import java.util.*;
import java.util.Map.*;
import java.util.regex.*;
import java.util.Collection.*;
import java.util.logging.*;

import java.io.*;

import javax.servlet.http.*;

import java.lang.Integer;
import java.lang.Long; 

// jsf-api classes
import javax.faces.component.*;
import javax.faces.component.UIComponent.*;
import javax.faces.component.html.*;
import javax.faces.event.*;
import javax.faces.context.*;
import javax.faces.FacesException;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import javax.faces.render.ResponseStateManager;

// new VDCRequestBean VDCRequestBean
import javax.ejb.EJB;

import edu.harvard.hmdc.vdcnet.study.DataTable;
import edu.harvard.hmdc.vdcnet.study.VariableServiceLocal;
import edu.harvard.hmdc.vdcnet.study.DataVariable;
import edu.harvard.hmdc.vdcnet.study.Study;
import edu.harvard.hmdc.vdcnet.study.SummaryStatistic;
import edu.harvard.hmdc.vdcnet.study.SummaryStatisticType;
import edu.harvard.hmdc.vdcnet.study.VariableCategory;
import edu.harvard.hmdc.vdcnet.study.StudyFile;

import edu.harvard.hmdc.vdcnet.web.common.VDCBaseBean;
import edu.harvard.hmdc.vdcnet.web.study.StudyUI;

import edu.harvard.hmdc.vdcnet.admin.VDCUser;
import edu.harvard.hmdc.vdcnet.admin.GroupServiceLocal;

import edu.harvard.hmdc.vdcnet.vdc.VDC;

import edu.harvard.hmdc.vdcnet.dsb.DSBWrapper;

// java studio creator's classes
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.*;

// the following package is deprecaged
// the new name is com.sun.rave.faces.data

import com.sun.jsfcl.data.*;

import edu.harvard.hmdc.vdcnet.dsb.*;
import edu.harvard.hmdc.vdcnet.dsb.impl.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.zip.*;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.*;


public class AnalysisPage extends VDCBaseBean implements java.io.Serializable {
    
    /**
     * Injects VariableServiceLocal to this class
     */
    @EJB
    private VariableServiceLocal variableService;
    @EJB
    private DvnDSBTimerServiceLocal dvnDSBTimerService;

    /**
     * getter for the injected VariableServiceLocal
     * 
     * @return VariableService
     */
    public VariableServiceLocal getVariableService() {
        return variableService;
    }

    // -----------------------------------------------------------------------
    // Initializes JSF components
    // -----------------------------------------------------------------------
    // <editor-fold desc="Initialization of JSF components">
    
    /*
     * sets various initial values of html components
     *
     */
    private void _init() throws Exception {
        
        // Advanced Statistics: radio button group for the setx option
        radioButtonGroup1DefaultOptions.setOptions(
            new Option[] {
                new Option("0", "Use average values (setx default)"),
                new Option("1", "Select values")
            });
            //  new Option("1","Set a value for 1 or 2 variables") 
            //  new Option("2","First differences between baseline and alterantive")
            
        
        simOptionMap.put("Use average values (setx default)","0");
        simOptionMap.put("Select values", "1");
        //radioSimTypeChoiceSelected = "0";
        radioSimTypeChoice.setValue("0");
        
        // Sets the default value of the above setx option
        radioButtonGroup1DefaultOptions.setSelectedValue("0");

        // Advanced Statistics: checkbox group for the output option pane
        checkboxGroup2DefaultOptions.setOptions(
            new Option[] {
                new Option("Summary", "Include Summary Statistics"),
                new Option("Plots", "Include Plot"),
                new Option("BinOutput", "Include Replication Data")
            });

        // Sets the default state of each checkbox of the above group
        checkboxGroup2DefaultOptions.setSelectedValue(new Object[] { "Summary",
            "Plots", "BinOutput" });


        chkbxAdvStatOutputOptMap.put("Include Summary Statistics", "Summary");
        chkbxAdvStatOutputOptMap.put("Include Plot", "Plots");
        chkbxAdvStatOutputOptMap.put("Include Replication Data", "BinOutput");
        
        chkbxAdvStatOutputOpt.setValue(new String[]{"Summary", "Plots", "BinOutput"});

        chkbxAdvStatOutputXtbOptMap.put("Include Totals","xtb_Totals");
        chkbxAdvStatOutputXtbOptMap.put("Include Statistics","xtb_Statistics");
        chkbxAdvStatOutputXtbOptMap.put("Include Percentages","xtb_Percentages");
        chkbxAdvStatOutputXtbOptMap.put("Include Extra Tables","xtb_ExtraTables");
        
        chkbxAdvStatOutputXtbOpt.setValue(new String[]{"xtb_Totals", "xtb_Statistics", 
                "xtb_Percentages",});
        
        // Advanced Statistics: checkbox group for the output option pane(xtab)
        checkboxGroupXtbOptions.setOptions(
            new Option[] {
                new Option("xtb_Totals",      "Include Totals"),
                new Option("xtb_Statistics",  "Include Statistics"),
                new Option("xtb_Percentages", "Include Percentages"),
                new Option("xtb_ExtraTables", "Include Extra Tables")
            });
        
        // Sets the default state of each checkbox of the above group
        checkboxGroupXtbOptions.setSelectedValue(
            new Object[] {
                "xtb_Totals", 
                "xtb_Statistics", 
                "xtb_Percentages",
                "false"
            });

        // Dropdown menu of how many rows to be displayed in the variable table
        howManyRowsOptions.setOptions(
            new Option[] {
                new Option("20", "20 Variables"), 
                new Option("10", "10 Variables"),
                new Option("50", "50 Variables"), 
                new Option("0", "All") 
            });
        
        // Sets the default state of the above Dropdown menu to 20 (variables)
        /*
        // howManyRowsOptions.setSelectedValue("20");
        */
        
        /*
         * Setter for property StudyUIclassName.  The instance of StudyUI class
         * contains citation-related information
         */
        setStudyUIclassName("edu.harvard.hmdc.vdcnet.web.study.StudyUI");
        
        /*
         * Fills dropdown menu for selecting a model (advanced Statistics tab)
         * with options obtained through AnalysisApplicationBean
         */
        setModelMenuOptions(getAnalysisApplicationBean().getModelMenuOptions());
        
        /*
         * Initializes the dataType-to-integer table used for
         * the switch statement in the checkVarType method
         */
        dataType2Int.put("binary", Integer.valueOf("1"));
        dataType2Int.put("nominal", Integer.valueOf("2"));
        dataType2Int.put("nominal|ordinal", Integer.valueOf("23"));
        dataType2Int.put("ordinal|nominal", Integer.valueOf("32"));
        dataType2Int.put("ordinal", Integer.valueOf("3"));
        dataType2Int.put("discrete", Integer.valueOf("4"));
        dataType2Int.put("continuous", Integer.valueOf("5"));
        dataType2Int.put("any", Integer.valueOf("0"));

        // Initializes the variable-type-to-String conversion table 
        vtInt2String.put("2", "continuous");
        vtInt2String.put("1", "discrete");
        vtInt2String.put("0", "character");
        
        
        getTabSet1().setSelected("tabDwnld");
        getVDCRequestBean().setSelectedTab("tabDwnld");
       
        REP_README_FILE = new File(this.getClass().getResource("README").getFile());
        REP_README_FILE_PREFIX = "README_";
        DVN_R2HTML_CSS_FILE = new File(this.getClass().getResource("R2HTML.css").getFile());
        DVN_R_HELPER_FILE = new File(this.getClass().getResource("dvn_helper.R").getFile());
    } // end of _init()

    // </editor-fold>

    // -----------------------------------------------------------------------
    // Static Variables
    // -----------------------------------------------------------------------
    // <editor-fold desc="Static Variables">

    /** 
     * The number of variables to be shown in the variable table.
     * The default value is set 20. 0 is used to show all variables
     */
    private static final int INITIAL_ROW_NO = 20;

    /** Sets the id number of the download GUI pane */
    private static final int PANE_DWNLD = 3;
    
    /** Sets the id number of the recode GUI pane */
    private static final int PANE_RECODE = 2;
    
    /** Sets the id number of the EDA GUI pane */
    private static final int PANE_EDA = 4;
    
    /** Sets the id number of the advanced statistics GUI pane */
    private static final int PANE_ADVSTAT = 5;
    
    private boolean resultPageTest = false;
    
    /** Sets the logger (use the package name) */
    private static Logger dbgLog = Logger.getLogger(AnalysisPage.class.getPackage().getName());
    
    private static String SUBSET_FILENAME_PREFIX="dvnSubsetFile.";
    
    private static File REP_README_FILE;
    private static String REP_README_FILE_PREFIX;
    
    private static File DVN_R2HTML_CSS_FILE;
    
    private static File DVN_R_HELPER_FILE;
    
    private static Long TEMP_FILE_LIFETIME=10L;
    
    // </editor-fold>
    
    // -----------------------------------------------------------------------
    // Non-JSF-component Instance variables
    // -----------------------------------------------------------------------
    // <editor-fold desc="Instance Variables">
    private List<File> deleteTempFileList = new ArrayList<File>();

    /**
     * Instance of the DataTable that contains major metadata of all variables
     * in the data file requested by an end-user
     */
    private edu.harvard.hmdc.vdcnet.study.DataTable dataTable;

    /**
     * The List object that holds the metadata of all variables in the
     * requested data file
     */
    private List<DataVariable> dataVariables = new ArrayList<DataVariable>();

    /**
     * The ID of the requested DataTable instance. Specified as a managed-property in
     * managed-beans.xml <property-class>java.lang.Long</property-class>
     * <value>#{param.dtId}</value>
     */
    private Long dtId;

    /**
     * Getter for property dtId
     * 
     * @return    value of property dtId
     */
    public Long getDtId() {
        return this.dtId;
    }

    /**
     * Setter for property dtId.
     * 
     * @param dtId    new value of property dtId
     *           
     */
    public void setDtId(Long dtId) {
        this.dtId = dtId;
    }

    /**
     * The Id of the data table that stores the requested data file
     */
    private String dataTableId;

    /**
     * The hash table (variable Id to variable name)
     * that stores the current selected variables
     */
    public Map<String, String> varCart = new LinkedHashMap<String, String>();

    // Map for switch statement in checkVarType()
    /**
     *  The dataType-to-integer table used for
     *  the switch statement in the checkVarType method
     */
    public Map<String, Integer> dataType2Int = new HashMap<String, Integer>();

    /**
     * The variable-type-to-String conversion table 
     */
    public Map<String, String> vtInt2String = new HashMap<String, String>();
    
    
    // study-related data
    /**
     * The name of StudyUI class that contains the citation-related 
     * information about the requested Study
     */
    private String studyUIclassName;
    
    /**
     * Setter for property studyUI class name
     * @param suiClass    studyUI classbname
     */
    public void setStudyUIclassName(String suiClass) {
        studyUIclassName = suiClass;
    }

    /**
     * getter for property studyUI class-name
     *
     * @return    the studyUI class name
     */
    public String getStudyUIclassName() {
        return studyUIclassName;
    }
    
    /** The citation information as a String */
    private String citation;

    /**
     * getter for property citation
     * 
     * @return    the citation information of the requested data file
     */
    public String getCitation() {
        return citation;
    }
    /**
     * Setter for property citation
     *
     * @param c    citation information as a String
     */
    public void setCitation(String c) {
        citation = c;
    }
    
    /** The title of the requested study */
    private String studyTitle;

    /**
     * getter for property studyTilte
     *
     * @return    the title of the requested study
     */
    public String getStudyTitle() {
        return studyTitle;
    }
    
    /**
     * setter for property studyTilte
     *
     * @param sTl   the tilte of the requested study
     */
    public void setStudyTitle(String sTl) {
        studyTitle = sTl;
    }
    
    /** The ID of the requested study */
    private Long studyId;

    /**
     * Getter for property studyId
     *
     * @return the ID of the requested study
     */
   public Long getStudyId() {
        return studyId;
    }
    
    /**
     * Setter for property studyId
     *
     * @param sId    the ID of the requested study
     */
    public void setStudyId(Long sId) {
        studyId = sId;
    }
    
    /** 
     * The name of the requested data file
     * Exposed to SubsettingPage.jsp
     */
    private String fileName;
    
    /**
     * Getter for property fileName
     *
     * @return    the name of the requested data file
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Setter for property fileName
     *
     * @param fNm    the name of the requested data file
     */
    public void setFileName(String fNm) {
        fileName = fNm;
    }
    
    /** The URL of the requested study */
    private String studyURL;
    
    /**
     * Getter for property studyURL
     *
     * @return    the URL of the requested study
     */
    public String getAtudyURL() {
        return studyURL;
    }
    
    /**
     * Setter for property studyURL
     *
     * @param url    the URL of the requested study
     */
    public void setStudyURL(String url) {
        studyURL = url;
    }

    /** The type of an end-user's browser */
    private String browserType;

    /**
     * Getter for property browserType
     *
     * @return    the type of an end-user's browser
     */
    public String getBrowserType() {
        return browserType;
    }
    /**
     * Setter for property borwserType
     *
     * @param bt    the type of an end-user's browser
     */
    public void setBrowserType(String bt) {
        this.browserType = bt;
    }

    /** The subsettability of the Subsetting Page */
    private Boolean subsettingPageAccess;
    
    /** Sets the state number of the initially selected tab.
     *  The default is 3 
     */
    private int clickedTab = 3;

    
    public Map<String, String> resultInfo = new HashMap<String, String>();
    
    public Map<String, String> getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(Map<String, String> resultInfo) {
        this.resultInfo = resultInfo;
    }    
    
    // </editor-fold>

    // -----------------------------------------------------------------------
    // Page-level JSF components
    // -----------------------------------------------------------------------

    // <editor-fold desc="Page-level-components">

    /**
     * ui:tabSet component backing the binding attribute of the tabset in
     * the subsetting page and whose id is tabSet1
     */
    private TabSet tabSet1 = new TabSet();

    /**
     * Getter for component tabSet1
     *
     * @return    the main tab-set component
     */

    public TabSet getTabSet1() {
        return tabSet1;
    }

    /**
     * Setter for component tabSet1
     *
     * @param ts    the main tab-set component
     */
    public void setTabSet1(TabSet ts) {
        tabSet1 = ts;
    }

    /**
     * ui:tab component backing the binding attribute of the tab of 
     * the downloading option and whose id is tabDwnld
     * 
     */
    private Tab tabDwnld = new Tab();
    
    /**
     * Getter for component tabDwnld
     *
     * @return    ui:tab of the downloading option
     */
    public Tab getTabDwnld() {
        return tabDwnld;
    }

    /**
     * Setter for component tabDwnld
     *
     * @param tab3    ui:tab of the downloading option
     */
    public void setTabDwnld(Tab tab3) {
        this.tabDwnld = tab3;
    }

    /**
     * ui:tab component backing the binding attribute of the tab of 
     * the recoding option and whose id is tabRecode
     */
    private Tab tabRecode = new Tab();

    /**
     * Getter for component tabRecode
     *
     * @return    ui:tab of the recoding option
     */
    public Tab getTabRecode() {
        return tabRecode;
    }

    /**
     * Setter for component tabRecode
     *
     * @param tab2    ui:tab of the recoding option
     */
    public void setTabRecode(Tab tab2) {
        this.tabRecode = tab2;
    }

    /**
     * ui:tab component backing the binding attribute of the tab of 
     * the EDA option and whose id is tabEda
     */
    private Tab tabEda = new Tab();

    /**
     * Getter for component tabEda
     *
     * @return    ui:tab of the EDA option
     */
    public Tab getTabEda() {
        return tabEda;
    }

    /**
     * Setter for component tabEda
     *
     * @param tab4    ui:tab of the EDA option
     */
    public void setTabEda(Tab tab4) {
        this.tabEda = tab4;
    }

    /**
     * ui:tab component backing the binding attribute of the tab of 
     * the advanced statistics optionn and whose id is tabAdvStat
     */
    private Tab tabAdvStat = new Tab();

    /**
     * Getter for component tabAdvStat
     *
     * @return    ui:tab of the advanced statistics option
     */
    public Tab getTabAdvStat() {
        return tabAdvStat;
    }

    /**
     * Setter for component tabAdvStat
     *
     * @param tab5    ui:tab of the advanced statistics option
     */
    public void setTabAdvStat(Tab tab5) {
        this.tabAdvStat = tab5;
    }


    private String tab;

    public String getTab() {
        return tab;
    }

    public void setTab(String tab) {
        if (tab == null || tab.equals("tabDwnld") || tab.equals("tabRecode") || 
         tab.equals("tabEda") || tab.equals("tabAdvStat")  ) {
            this.tab = tab;
        }
    }






    /** The Id of the currently selected tab */
    private String currentTabId;

    /**
     * Getter for property currentTabId
     *
     * @return    the Id of the currently selected tab
     */
    public String getCurrentTabId() {
        return currentTabId;
    }
    /**
     * Setter for property currentTabId
     *
     * @param tb    the Id of the currently selected tab
     */
    public void setCurrentTabId(String tb) {
        currentTabId = tb;
    }
    
    /**
     * Moves all variables back to the box of an end-user selected variables
     * and resets the backing storage object varCart.  
     * Backing the actionListener attribute of each ui:tab component.
     *
     * @param  acev    tab-clicking-action event
     */
    public void resetVariableInLBox(ActionEvent acev) {
        dbgLog.fine("Within resetVariableInLBox: tab Id ="
            + acev.getComponent().getId());
        getVDCRequestBean().setSelectedTab(acev.getComponent().getId());
        // remove vars from RHS boxes
        advStatVarRBox1.clear();
        advStatVarRBox2.clear();
        advStatVarRBox3.clear();

        // add existing vars to LHS box
        // add user-defined vars to LHS box if available
        resetVarSetAdvStat(varCart);
        
        // recode message area
        resetMsgSaveRecodeBttn();
        resetMsg4MoveVar();
        
        // download message area
        msgDwnldButton.setText(" ");
        msgDwnldButton.setVisible(false);

        
        // eda message area
        msgEdaButton.setText(" ");
        msgEdaButton.setVisible(false);
        
        // advance stat area
        msgAdvStatButton.setText(" ");
        msgAdvStatButton.setVisible(false);
    }

    // </editor-fold>

    // -----------------------------------------------------------------------
    // download section   
    // -----------------------------------------------------------------------
    // <editor-fold desc="download">

    // download radio button selection
    private HtmlSelectOneRadio dwnldFileTypeSet = new HtmlSelectOneRadio();

    public HtmlSelectOneRadio getDwnldFileTypeSet() {
        return dwnldFileTypeSet;
    }

    public void setDwnldFileTypeSet(HtmlSelectOneRadio hsor) {
        this.dwnldFileTypeSet = hsor;
    }

    // download: radio button items

    private List dwnldFileTypeItems = null;

    public List getDwnldFileTypeItems() {
        if (dwnldFileTypeItems == null) {
            dwnldFileTypeItems = new ArrayList();
            dwnldFileTypeItems.add("D01");// Text
            dwnldFileTypeItems.add("D04");// RData
            dwnldFileTypeItems.add("D02");// Splus
            dwnldFileTypeItems.add("D03");// Stata
        }
        return dwnldFileTypeItems;
    }

    // dwnldBttn:h:commandButton@binding
    private HtmlCommandButton dwnldButton = new HtmlCommandButton();

    public HtmlCommandButton getDwnldButton() {
        return dwnldButton;
    }

    public void setDwnldButton(HtmlCommandButton hcb) {
        this.dwnldButton = hcb;
    }
    
    // dwnldBttn:h:commandButton@action
    public boolean checkDwnldParameters (){
        boolean result=true;
        dbgLog.fine("***** within checkDwnldParameters() *****");
        // param-checking conditions
        if (dwnldFileTypeSet.getValue()== null) {
            dbgLog.warning("download radio button set: no selected value");
            result=false;
        } else {
            dbgLog.fine("download radio button set: selected value"+(String)dwnldFileTypeSet.getValue());
        }
         return result;
    }

    HtmlPanelGroup pgDwnldErrMsg = new HtmlPanelGroup();

    public HtmlPanelGroup getPgDwnldErrMsg() {
        return pgDwnldErrMsg;
    }

    public void setPgDwnldErrMsg(HtmlPanelGroup pgDwnldErrMsg) {
        this.pgDwnldErrMsg = pgDwnldErrMsg;
    }
    

    // msgDwnldButton:ui:StaticText@binding
    private StaticText msgDwnldButton = new StaticText();

    public StaticText getMsgDwnldButton() {
        return msgDwnldButton;
    }

    public void setMsgDwnldButton(StaticText txt) {
        this.msgDwnldButton = txt;
    }
    
    /**
     * The object backing the value attribute of
     * ui:StaticText for msgDwnldButton that shows 
     * error messages for the action of the download
     * button
     */
    private String msgDwnldButtonTxt;

    /**
     * Getter for property msgDwnldButtonTxt
     *
     * @return the error message text
     */
    public String getMsgDwnldButtonTxt() {
        return msgDwnldButtonTxt;
    }
    /**
     * Setter for property msgDwnldButtonTxt
     *
     * @param txt   the new error message text
     */
    public void setMsgDwnldButtonTxt(String txt) {
        this.msgDwnldButtonTxt = txt;
    }
    
    /**
     * Clears the contents of the error message text (msgDwnldButtonTxt)
     * for the download button (dwnldButton)
     */
    public void resetMsgDwnldButton() {
        dbgLog.fine("***** within resetMsgDwnldButton *****");
        // Replaces the error message text with spaces
        // so that the previous error message is not shown
        // even if the following setVisible(false) line fails
        msgDwnldButton.setText("     ");
        
        // Stores the new state of msgDwnldButtonTxt in the session map
        FacesContext.getCurrentInstance().getExternalContext()
            .getSessionMap().put("msgDwnldButtonTxt", msgDwnldButtonTxt);
        
        // Hides the error message text next to the download button
        msgDwnldButton.setVisible(false);
        pgDwnldErrMsg.setRendered(false);
        dbgLog.fine("***** resetMsgDwnldButton: end *****");
    }


    // end of download section -----------------------------------------------
    // </editor-fold>

    
    // dwnldButton:h:commandButton@action
    public String dwnldAction() {
    
        resetMsgDwnldButton();
        
        if (checkDwnldParameters()) {
        
            FacesContext cntxt = FacesContext.getCurrentInstance();

            HttpServletResponse res = (HttpServletResponse) cntxt
                .getExternalContext().getResponse();
                
            HttpServletRequest req = (HttpServletRequest) cntxt
                .getExternalContext().getRequest();


            dbgLog.fine("***** within dwnldAction() *****");

            StudyFile sf = dataTable.getStudyFile();
            Long noRecords = dataTable.getRecordsPerCase();

            String dsbUrl = getDsbUrl();
            dbgLog.fine("dsbUrl=" + dsbUrl);

            String serverPrefix = req.getScheme() + "://"
                + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath();

            dbgLog.fine("serverPrefix"+serverPrefix);

            Map<String, List<String>> mpl = new HashMap<String, List<String>>();

            // String formatType = req.getParameter("formatType");
            String formatType = (String) dwnldFileTypeSet.getValue();
            dbgLog.fine("file type from the binding=" + formatType);

            mpl.put("dtdwnld", Arrays.asList(formatType));

            // if there is a user-defined (recoded) variables
            /*
            if (recodedVarSet.size() > 0) {
                mpl.putAll(getRecodedVarParameters());
            }
            */
            
            dbgLog.fine("citation info to be sent:\n" + citation);

            mpl.put("studytitle", Arrays.asList(studyTitle));
            mpl.put("studyno", Arrays.asList(studyId.toString()));
            mpl.put("studyURL", Arrays.asList(studyURL));
            mpl.put("browserType", Arrays.asList(browserType));

            mpl.put("recodedVarIdSet", getRecodedVarIdSet());
            mpl.put("recodedVarNameSet",getRecodedVarNameSet());
            mpl.put("recodedVarLabelSet", getRecodedVarLabelSet());
            mpl.put("recodedVarTypeSet", getRecodedVariableType());
            mpl.put("recodedVarBaseTypeSet", getBaseVariableTypeForRecodedVariable());


            mpl.put("baseVarIdSet",getBaseVarIdSetFromRecodedVarIdSet());
            mpl.put("baseVarNameSet",getBaseVarNameSetFromRecodedVarIdSet());
                        
            mpl.put("requestType", Arrays.asList("Download"));

            // -----------------------------------------------------
            // New processing route
            // 
            // Step 0. Locate the data file and its attributes
    
            String fileId = sf.getId().toString();
            //String fileURL = serverPrefix + "/FileDownload/?fileId=" + fileId + "&isSSR=1&xff=0&noVarHeader=1";
            String fileURL = "http://localhost:" + req.getServerPort() + "/dvn" + "/FileDownload/?fileId=" + fileId + "&isSSR=1&xff=0&noVarHeader=1";

            dbgLog.fine("fileURL="+fileURL);
            
            String fileloc = sf.getFileSystemLocation();
            String tabflnm = sf.getFileName();
            boolean sbstOK = sf.isSubsettable();
            String flct = sf.getFileType();
            
            dbgLog.fine("location="+fileloc);
            dbgLog.fine("filename="+tabflnm);
            dbgLog.fine("subsettable="+sbstOK);
            dbgLog.fine("filetype="+flct);

            DvnRJobRequest sro = null;
             
            List<File> zipFileList = new ArrayList();
            
           // the data file for downloading/statistical analyses must be subset-ready
            // local (relative to the application) file case 
            // note: a typical remote case is: US Census Bureau
            

            
            File tmpsbfl= null;
            
            if (sbstOK){
                
                try {

                // Step 1. temporarily store the whole data set in a temp directory

                    // Create a URL for the data file
                    URL url = new URL(fileURL);

                    // temp data file that stores incoming data from the above URL
                    File tmpfl = File.createTempFile("tempTabfile.", ".tab");
                    deleteTempFileList.add(tmpfl);
                    // temp subset file that stores requested variables 
                    tmpsbfl = File.createTempFile("tempsubsetfile.", ".tab");
                    deleteTempFileList.add(tmpsbfl);
                    //zipFileList.add(tmpsbfl);
                    
                    // Typical file-copy idiom 
                    // incoming/outgoing streams
                    InputStream inb = new BufferedInputStream(url.openStream());
                    OutputStream outb = new BufferedOutputStream(new FileOutputStream(tmpfl));

                    int bufsize;
                    byte [] bffr = new byte[8192];
                    while ((bufsize = inb.read(bffr))!=-1) {
                        outb.write(bffr, 0, bufsize);
                    }
                    
                    inb.close();
                    outb.close();
                    
                    // Checks the obtained data file
                    if (tmpfl.exists()){
                        Long wholeFileSize = tmpfl.length();
                        dbgLog.fine("whole file:length="+wholeFileSize);
                        dbgLog.fine("tmp file:name="+tmpfl.getAbsolutePath());
                        
                        if (wholeFileSize <= 0){
                            // subset file exists but it is empty
                        
                            msgDwnldButton.setText("* an data file is empty");
                            msgDwnldButton.setVisible(true);
                            dbgLog.warning("exiting dwnldAction() due to a file access error:"+
                            "a data file is empty"
                            );
                            getVDCRequestBean().setSelectedTab("tabDwnld");
                            dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                            return "failure";
                        }
                        
                    } else {
                        // file was not created/downloaded
                        msgDwnldButton.setText("* a data file was not created");
                        msgDwnldButton.setVisible(true);
                        dbgLog.warning("exiting dwnldAction() due to a file access error:"+
                        "a data file was not created"
                        );
                        getVDCRequestBean().setSelectedTab("tabDwnld");

                        return "failure";
                    }

                    // source data file: full-path name
                    String cutOp1 = tmpfl.getAbsolutePath();

                    // result(subset) data file: full-path name
                    String cutOp2 = tmpsbfl.getAbsolutePath();
                    
                    // check whether a source file is tab-delimited or not
                    boolean fieldcut = true;
                    if ((noRecords != null) && (noRecords >=1)){
                        fieldcut = false;
                    }

                    if (fieldcut){
                // Step 2.a. Set-up parameters for subsetting: cutting requested fields of data
                        // from a temp (whole) delimited file

                        Set<Integer> fields = getFieldNumbersForSubsetting();
                         dbgLog.fine("fields="+fields);

                        // Create an instance of DvnJavaFieldCutter
                        FieldCutter fc = new DvnJavaFieldCutter();

                        // Executes the subsetting request
                        fc.subsetFile(cutOp1, cutOp2, fields);


                    } else {
                // Step 2.b. Set-up parameters for subsetting: cutting requested columns of data
                        // from a temp (whole) non-delimited file

                        // Using new, native implementation of fixed-field cutting 
                        // (instead of executing rcut in a shell)

                        Map<Long, List<List<Integer>>> varMetaSet = getSubsettingMetaData(); 
                        DvnNewJavaFieldCutter fc = new DvnNewJavaFieldCutter(varMetaSet);

                        try {
                            fc.cutColumns(new File(cutOp1), varMetaSet.size(), 0, "\t", cutOp2);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();

                            msgDwnldButton.setText("* could not generate subset due to an IO problem");
                            msgDwnldButton.setVisible(true); 
                            dbgLog.warning("exiting dwnldAction() due to an IO problem ");
                            getVDCRequestBean().setSelectedTab("tabDwnld");
                            dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                            return "failure";

                        } catch (RuntimeException re){
                            re.printStackTrace();
                            
                            msgDwnldButton.setText("* could not generate subset due to an runtime error");
                            msgDwnldButton.setVisible(true); 
                            dbgLog.warning("exiting dwnldAction() due to an runtime error");
                            getVDCRequestBean().setSelectedTab("tabDwnld");
                            dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                            return "failure";
                            
                        }
                        // end: non-delimited case
                    }
                    
                    // Checks the resulting subset file 
                    if (tmpsbfl.exists()){
                        Long subsetFileSize = tmpsbfl.length();
                        dbgLog.fine("subset file:Length="+subsetFileSize);
                        dbgLog.fine("subset file:name="+tmpsbfl.getAbsolutePath());
                        
                        if (subsetFileSize > 0){
                            mpl.put("subsetFileName", Arrays.asList(cutOp2));
                            mpl.put("subsetDataFileName",Arrays.asList(tmpsbfl.getName()));
                        } else {
                            // subset file exists but it is empty
                        
                            msgDwnldButton.setText("* an subset file is empty");
                            msgDwnldButton.setVisible(true);
                            dbgLog.warning("exiting dwnldAction() due to a subsetting error:"+
                            "a subset file is empty"
                            );
                            getVDCRequestBean().setSelectedTab("tabDwnld");
                            dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                            return "failure";

                        }
                    } else {
                        // subset file was not created
                        msgDwnldButton.setText("* a subset file was not created");
                        msgDwnldButton.setVisible(true);
                        dbgLog.warning("exiting dwnldAction() due to a subsetting error:"+
                        "a subset file was not created"
                        );
                        getVDCRequestBean().setSelectedTab("tabDwnld");
                        dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                        return "failure";

                    }
                    
                // Step 3. Organizes parameters/metadata to be sent to the implemented
                    // data-analysis-service class
                    
                    //Map<String, Map<String, String>> vls = getValueTableForRequestedVariables(getDataVariableForRequest());
                    Map<String, Map<String, String>> vls = getValueTablesForAllRequestedVariables();
                    
                    sro = new DvnRJobRequest(getDataVariableForRequest(), mpl, vls, recodeSchema);

                    dbgLog.fine("sro dump:\n"+ToStringBuilder.reflectionToString(sro, ToStringStyle.MULTI_LINE_STYLE));
                    
                // Step 4. Creates an instance of the the implemented 
                    // data-analysis-service class 

                    DvnRDataAnalysisServiceImpl das = new DvnRDataAnalysisServiceImpl();

                    // Executes a request of downloading or data analysis and 
                    // capture resulting information as a Map <String, String>

                    resultInfo = das.execute(sro);
                    

                // Step 5. Checks the DSB-exit-status code
                    if (resultInfo.get("RexecError").equals("true")){
                    
                        msgDwnldButton.setText("* The Request failed due to an R-runtime error");
                        msgDwnldButton.setVisible(true);
                        dbgLog.fine("exiting dwnldAction() due to an R-runtime error");
                        getVDCRequestBean().setSelectedTab("tabDwnld");
                        dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                        return "failure";
                    } else {
                        if (recodeSchema.size()> 0){
                            resultInfo.put("subsettingCriteria",sro.getSubsetConditionsForCitation());
                        } else {
                            resultInfo.put("subsettingCriteria","");
                        }
                    }
                    
                } catch (MalformedURLException e) {
                    e.printStackTrace();

                    msgDwnldButton.setText("* file URL is malformed");
                    msgDwnldButton.setVisible(true);
                    dbgLog.warning("exiting dwnldAction() due to a URL problem ");
                    getVDCRequestBean().setSelectedTab("tabDwnld");
                    
                    return "failure";

                } catch (IOException e) {
                    // this may occur if the dataverse is not released
                    // the file exists, but it is not accessible 
                    e.printStackTrace();
                    
                    msgDwnldButton.setText("* an IO problem occurred");
                    msgDwnldButton.setVisible(true);
                    dbgLog.warning("exiting dwnldAction() due to an IO problem ");
                    getVDCRequestBean().setSelectedTab("tabDwnld");

                    return "failure";
                }
                
               // end of subset-OK case
            } else {
                // not subsettable data file
                msgDwnldButton.setText("* this data file is not subsettable file");
                msgDwnldButton.setVisible(true);
                dbgLog.warning("exiting dwnldAction(): the data file is not subsettable ");
                getVDCRequestBean().setSelectedTab("tabDwnld");
                dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                return "failure";

            } // end:subsetNotOKcase
            
            // final processing steps for all successful cases
            
            // add stuy-metadata to the resultInfo map
            resultInfo.put("offlineCitation", citation);
            resultInfo.put("studyTitle", studyTitle);
            resultInfo.put("studyNo", studyId.toString());
            resultInfo.put("studyURL", studyURL);
            resultInfo.put("R_min_verion_no","2.7.0");
            resultInfo.put("dataverse_version_no","1.3");
            
            dbgLog.fine("wbDataFileName="+resultInfo.get("wbDataFileName"));
            dbgLog.fine("RwrkspFileName="+resultInfo.get("wrkspFileName"));
            
            // writing necessary files
            
            // files to be written back as a zip file
            //           local     remote  local      local     remote
            // tab       citation, data,   R history, codefiles, wrksp 
            // others    citation, data,   R history,            wrksp
            
            try{

                // rename the subsetting file
                File tmpsbflnew = File.createTempFile(SUBSET_FILENAME_PREFIX + resultInfo.get("PID") +".", ".tab");
                deleteTempFileList.add(tmpsbflnew);
                InputStream inb = new BufferedInputStream(new FileInputStream(tmpsbfl));
                OutputStream outb = new BufferedOutputStream(new FileOutputStream(tmpsbflnew));

                int bufsize;
                byte [] bffr = new byte[8192];
                while ((bufsize = inb.read(bffr))!=-1) {
                    outb.write(bffr, 0, bufsize);
                }
                inb.close();
                outb.close();
                
                
                String rhistNew = StringUtils.replace(resultInfo.get("RCommandHistory"), tmpsbfl.getName(),tmpsbflnew.getName());
                
                
                //zipFileList.add(tmpsbflnew);

                // (1) write a citation file 
//                String citationFilePrefix = "citationFile_"+ resultInfo.get("PID") + "_";
//                File tmpcfl = File.createTempFile(citationFilePrefix, ".txt");
//
//                zipFileList.add(tmpcfl);
//                deleteTempFileList.add(tmpcfl);
//
//                DvnCitationFileWriter dcfw = new DvnCitationFileWriter(resultInfo);
//
//                String fmpcflFullname = tmpcfl.getAbsolutePath();
//                String fmpcflname = tmpcfl.getName();
//                dcfw.write(tmpcfl);

                // write a R command file
                //String rhistoryFilePrefix = "RcommandFile_" + resultInfo.get("PID") + "_";
                //File tmpRhfl = File.createTempFile(rhistoryFilePrefix, ".R");

                //zipFileList.add(tmpRhfl);
                //deleteTempFileList.add(tmpRhfl);
                
                //writeRhistory(tmpRhfl, rhistNew);
                
                // (2) tab-delimitd-format-only step
                if (formatType.equals("D01")){
                    // write code files
                    String codeFilePrefix = "codeFile_" + resultInfo.get("PID") + "_";
                    
                    File tmpCCsasfl = File.createTempFile(codeFilePrefix, ".sas");
                    deleteTempFileList.add(tmpCCsasfl);
                    zipFileList.add(tmpCCsasfl);

                    File tmpCCspsfl = File.createTempFile(codeFilePrefix, ".sps");
                    deleteTempFileList.add(tmpCCspsfl);
                    zipFileList.add(tmpCCspsfl);


                    File tmpCCdofl  = File.createTempFile(codeFilePrefix, ".do");
                    deleteTempFileList.add(tmpCCdofl);
                    zipFileList.add(tmpCCdofl);

                    StatisticalCodeFileWriter scfw = new StatisticalCodeFileWriter(sro);
                    scfw.write(tmpCCsasfl, tmpCCspsfl, tmpCCdofl);

                }
                
                // (2)The format-converted subset data file
                // get the path-name of the data-file to be delivered 
                String wbDataFileName = resultInfo.get("wbDataFileName");
                dbgLog.fine("wbDataFileName="+wbDataFileName);
                
                File wbSubsetDataFile = new File(wbDataFileName);
                if (wbSubsetDataFile.exists()){
                    dbgLog.fine("wbSubsetDataFile:length="+wbSubsetDataFile.length());
                    deleteTempFileList.add(wbSubsetDataFile);
                    zipFileList.add(wbSubsetDataFile);

                } else {
                    // the data file was not created
                    dbgLog.fine("wbSubsetDataFile does not exist");

                    msgDwnldButton.setText("* The requested data file is not available");
                    msgDwnldButton.setVisible(true);
                    dbgLog.warning("exiting dwnldAction(): data file was not transferred");
                    getVDCRequestBean().setSelectedTab("tabDwnld");
                    dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                    return "failure";
                }
                
                // R-work space file
//                String wrkspFileName = resultInfo.get("wrkspFileName");
//                dbgLog.fine("wrkspFileName="+wrkspFileName);
//                
//                File RwrkspFileName = new File(wrkspFileName);
//                if (RwrkspFileName.exists()){
//                    dbgLog.fine("RwrkspFileName:length="+RwrkspFileName.length());
//
//                    //zipFileList.add(RwrkspFileName);
//
//                } else {
//                    dbgLog.fine("RwrkspFileName does not exist");
//                    //msgDwnldButton.setText("* The workspace file is not available");
//                    //msgDwnldButton.setVisible(true);
//                    dbgLog.warning("dwnldAction(): R workspace file was not transferred");
//                    //getVDCRequestBean().setSelectedTab("tabDwnld");
//
//                    //return "failure";
//                }
//                deleteTempFileList.add(RwrkspFileName);

//                // vdc_startup.R file
//                String vdc_startupFileName = resultInfo.get("vdc_startupFileName");
//                dbgLog.fine("vdc_startupFileName="+vdc_startupFileName);
//                File vdcstrtFileName = new File(vdc_startupFileName);
//                if (vdcstrtFileName.exists()){
//                    dbgLog.fine("vdcstrtFileName:length="+vdcstrtFileName.length());
//                    zipFileList.add(vdcstrtFileName);
//                } else {
//                    dbgLog.fine("vdcstrtFileName does not exist");
//                    //msgDwnldButton.setText("* vdc_startup.R is not available");
//                    //msgDwnldButton.setVisible(true);
//                    dbgLog.warning("dwnldAction(): vdc_startup.R was not transferred");
//                    //getVDCRequestBean().setSelectedTab("tabDwnld");
//
//                    //return "failure";
//                }
//                deleteTempFileList.add(vdcstrtFileName);
                
                // (3) add replication readme file
                //zipFileList.add(REP_README_FILE);
                File readMeFile = File.createTempFile(REP_README_FILE_PREFIX + resultInfo.get("PID") +"_", ".txt");
                DvnReplicationREADMEFileWriter rw = new DvnReplicationREADMEFileWriter(resultInfo);
                rw.writeREADMEfile(readMeFile);
                //zipFileList.add(REP_README_FILE);
                zipFileList.add(readMeFile);
                deleteTempFileList.add(readMeFile);
                    
                for (File f : zipFileList){
                    dbgLog.fine("path="+f.getAbsolutePath() +"\tname="+ f.getName());
                }


                // zipping all required files
                try{
                    String zipFilePrefix = "zipFile_" + resultInfo.get("PID") + "_";
                    File zipFile  = File.createTempFile(zipFilePrefix, ".zip");
                    deleteTempFileList.add(zipFile);
                    res.setContentType("application/zip");
                    String zfname = zipFile.getName();
                    res.setHeader("content-disposition", "attachment; filename=" + zfname);
                    
                    zipFiles(res.getOutputStream(), zipFileList);

                    FacesContext.getCurrentInstance().responseComplete();

            

                    // put resultInfo into the session object
                    // this step is unnecessary now because
                    //  no transition to the result page
                    //FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("resultInfo", resultInfo);

                    dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                    
                    dbgLog.fine("***** within dwnldAction(): ends here *****");
                                       
                    return "download";

                } catch (IOException e){
                    // file-access problem, etc.
                    e.printStackTrace();
                    dbgLog.fine("download zipping IO exception");
                    msgDwnldButton.setText("* an IO problem occurred");
                    msgDwnldButton.setVisible(true);
                    dbgLog.warning("exiting dwnldAction() due to an IO problem ");
                    getVDCRequestBean().setSelectedTab("tabDwnld");
                    dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                    return "failure";
                }
                // end of zipping step

            } catch (IOException e){
                e.printStackTrace();
                
                msgDwnldButton.setText("* an IO problem occurred");
                msgDwnldButton.setVisible(true);
                dbgLog.warning("exiting dwnldAction() due to an IO problem ");
                getVDCRequestBean().setSelectedTab("tabDwnld");
                dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                return "failure";
            }
            
            
            // end: params are OK-case
        } else {
            // the selection is incomplete
            // show error message;
            pgDwnldErrMsg.setRendered(true);
            msgDwnldButton.setText("* Error: Select a file format");
            msgDwnldButton.setVisible(true);
            dbgLog.warning("exiting dwnldAction() due to incomplete data ");
            getVDCRequestBean().setSelectedTab("tabDwnld");
            
            return "failure";
        } // end: checking params
        
        
    }

    
    
    // -----------------------------------------------------------------------
    // recode section
    // -----------------------------------------------------------------------
    // <editor-fold desc="recode">

    // ui-listbox-based solution

    // moveRecodeVarBttn:h:commandButton@binding
    private HtmlCommandButton moveRecodeVarBttn = new HtmlCommandButton();

    public HtmlCommandButton getMoveRecodeVarBttn() {
        return moveRecodeVarBttn;
    }

    public void setMoveRecodeVarBttn(HtmlCommandButton hcb) {
        this.moveRecodeVarBttn = hcb;
    }
    private PanelGroup groupPanelRecodeNewVarInfo = new PanelGroup();

    public PanelGroup getGroupPanelRecodeNewVarInfo() {
        return groupPanelRecodeNewVarInfo;
    }

    public void setGroupPanelRecodeNewVarInfo(PanelGroup groupPanelRecodeNewVarInfo) {
        this.groupPanelRecodeNewVarInfo = groupPanelRecodeNewVarInfo;
    }

    private HtmlPanelGroup groupPanelRecodeInstruction1 = new HtmlPanelGroup();

    public HtmlPanelGroup getGroupPanelRecodeInstruction1() {
        return groupPanelRecodeInstruction1;
    }

    public void setGroupPanelRecodeInstruction1(HtmlPanelGroup groupPanelRecodeInstruction1) {
        this.groupPanelRecodeInstruction1 = groupPanelRecodeInstruction1;
    }
    
    
    private HtmlPanelGroup groupPanelRecodeInstruction2 = new HtmlPanelGroup();

    public HtmlPanelGroup getGroupPanelRecodeInstruction2() {
        return groupPanelRecodeInstruction2;
    }

    public void setGroupPanelRecodeInstruction2(HtmlPanelGroup groupPanelRecodeInstruction2) {
        this.groupPanelRecodeInstruction2 = groupPanelRecodeInstruction2;
    }
    
    private HtmlPanelGroup groupPanelRecodeTableArea = new HtmlPanelGroup();

    public HtmlPanelGroup getGroupPanelRecodeTableArea() {
        return groupPanelRecodeTableArea;
    }

    public void setGroupPanelRecodeTableArea(HtmlPanelGroup groupPanelRecodeTableArea) {
        this.groupPanelRecodeTableArea = groupPanelRecodeTableArea;
    }
    
    // moveRecodeVarBttn:h:commandButton@actionListener
    public void moveRecodeVariable(ActionEvent acev) {

        dbgLog.fine("***** moveRecodeVariable(): begins here *****");
        if (!groupPanelRecodeTableArea.isRendered()){
            groupPanelRecodeTableArea.setRendered(true);
            groupPanelRecodeInstruction2.setRendered(true);
            groupPanelRecodeInstruction1.setRendered(false);
        }
        String varId = null;// getSelectedRecodeVariable();
        if (getSelectedRecodeVariable() == null){
            varId = (String)listboxRecode.getSelected();
        } else {
            varId = getSelectedRecodeVariable();
        }
        dbgLog.fine("recode-variable id=" + varId);
        dbgLog.fine("selected from the binding="+listboxRecode.getSelected());
        dbgLog.fine("Is this a recoded var?[" + isRecodedVar(varId) + "]");
        resetMsgSaveRecodeBttn();
        if (isRecodedVar(varId)) {
            // newly recoded var case
            dbgLog.fine("This a recoded var[" + varId + "]");

            List<Object> rvs = getRecodedVarSetRow(varId);
            if (rvs != null) {

                dbgLog.fine("requested newly recoded var was found");
                dbgLog.fine("new varName=" + rvs.get(0));
                dbgLog.fine("new varLabel=" + rvs.get(1));

                recallRecodedVariable(varId);

            } else {
                dbgLog.fine("requested newly recoded var was not found="
                    + varId);

            }

        } else {
            if (varId != null) {
                // set the name
                String varName = getVariableNamefromId(varId);
                dbgLog.fine("recode variable Name=" + varName);
                // setRecodeVariableName(varName);

                setCurrentRecodeVariableName(varName);
                setCurrentRecodeVariableId(varId);
                // set the label
                // setRecodeVariableLabel(getVariableLabelfromId(varId));
                String newNamePrefix = "new_"+ RandomStringUtils.randomAlphanumeric(2) +"_";
                recodeTargetVarName.setValue(newNamePrefix+varName);
                recodeTargetVarLabel.setValue(newNamePrefix+getVariableLabelfromId(varId));

                // get value/label data and set them to the table
                DataVariable dv = getVariableById(varId);

                Collection<VariableCategory> catStat = dv.getCategories();
                recodeDataList.clear();
                dbgLog.fine("catStat.size=" + catStat.size());
                if (catStat.size() > 0) {
                    // catStat exists
                    dbgLog.fine("catStat exists");

                    for (Iterator elc = catStat.iterator(); elc.hasNext();) {
                        VariableCategory dvcat = (VariableCategory) elc.next();
                        List<Object> rw = new ArrayList<Object>();

                        // 0th: Drop
                        rw.add(new Boolean(false));
                        // 1st: New value
                        rw.add(dvcat.getValue());
                        // 2nd: New value label
                        rw.add(dvcat.getLabel());
                        // conditions
                        rw.add(dvcat.getValue());
                        //
                        recodeDataList.add(rw);
                    }

                } else {
                    // no catStat, either sumStat or too-many-categories
                    dbgLog.fine("catStat does not exists");
                    dbgLog.fine("create a default two-row table");
                    for (int i = 0; i < 2; i++) {
                        List<Object> rw = new ArrayList<Object>();
                        // 0th: Drop
                        rw.add(new Boolean(false));
                        // 1st: New value
                        rw.add("enter value here");
                        // 2nd: New value label
                        rw.add("enter label here");
                        // conditions
                        rw.add("enter a condition here");
                        //
                        recodeDataList.add(rw);
                    }

                }

                // show the recodeTable
                dbgLog.fine("Number of rows in this Recode Table="
                    + recodeDataList.size());
                groupPanelRecodeTableHelp.setRendered(true);
                recodeTable.setRendered(true);
                addValueRangeBttn.setRendered(true);
                // keep this variable's Id
                FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("currentRecodeVariableId",
                        currentRecodeVariableId);
                FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("currentRecodeVariableName",
                        currentRecodeVariableName);

            } else {
                dbgLog.fine("Variable to be recoded is null");
            }
        }
        dbgLog.fine("***** moveRecodeVariable(): ends here *****");

    }

    public void clearRecodeTargetVarInfo() {
        dbgLog.fine("pass the clear step");
        recodeTargetVarName.resetValue();
        recodeTargetVarLabel.resetValue();
    }

    // panel above the recodeTable : recodeTableHelpPanel
    // @binding
    private PanelGroup groupPanelRecodeTableHelp = new PanelGroup();

    public PanelGroup getGroupPanelRecodeTableHelp() {
        return groupPanelRecodeTableHelp;
    }

    public void setGroupPanelRecodeTableHelp(PanelGroup pg) {
        this.groupPanelRecodeTableHelp = pg;
    }

    // checkbox column(drop this value)
    // recodeDropValueCheckbox@binding
    private Checkbox recodeDropValueCheckbox = new Checkbox();

    public Checkbox getRecodeDropValueCheckbox() {
        return recodeDropValueCheckbox;
    }

    public void setRecodeDropValueCheckbox(Checkbox c) {
        this.recodeDropValueCheckbox = c;
    }

    private HtmlSelectBooleanCheckbox recodeDropValueCheckboxx = new HtmlSelectBooleanCheckbox();
    
    public HtmlSelectBooleanCheckbox getRecodeDropValueCheckboxx() {
        return recodeDropValueCheckboxx;
    }

    public void setRecodeDropValueCheckboxx(HtmlSelectBooleanCheckbox cb) {
        this.recodeDropValueCheckboxx = cb;
    }    
    
    // listboxRecode:ui:listbox@binding => varSetAdvStat
    // boxL1:listbox@binding
    private Listbox listboxRecode = new Listbox();

    public Listbox getListboxRecode() {
        return listboxRecode;
    }

    public void setListboxRecode(Listbox lstbx) {
        this.listboxRecode = lstbx;
    }
    // listboxRecode:ui:listbox@selected
    private String selectedRecodeVariable;

    public void setSelectedRecodeVariable(String s) {
        selectedRecodeVariable = s;
    }

    public String getSelectedRecodeVariable() {
        return selectedRecodeVariable;
    }

    // recode-Variable-name for the recode-table header cell
    // h:outputText(recodeHdrVariable)
    // @value

    private String currentRecodeVariableName;

    public void setCurrentRecodeVariableName(String crv) {
        this.currentRecodeVariableName = crv;
    }

    public String getCurrentRecodeVariableName() {
        return currentRecodeVariableName;
    }

    private String currentRecodeVariableId;

    public void setCurrentRecodeVariableId(String crv) {
        this.currentRecodeVariableId = crv;
    }

    public String getCurrentRecodeVariableId() {
        return currentRecodeVariableId;
    }

    // h:inputText: recodeTargetVarName:
    // @binding
    private HtmlInputText recodeTargetVarName = new HtmlInputText();

    public HtmlInputText getRecodeTargetVarName() {
        return recodeTargetVarName;
    }

    public void setRecodeTargetVarName(HtmlInputText vn) {
        this.recodeTargetVarName = vn;
    }

    // @value
    private String recodeVariableName;

    public void setRecodeVariableName(String vn) {
        recodeVariableName = vn;
    }

    public String getRecodeVariableName() {
        return recodeVariableName;
    }

    // h:inputText: recodeTargetVarLabel
    // @binding
    private HtmlInputText recodeTargetVarLabel = new HtmlInputText();

    public HtmlInputText getRecodeTargetVarLabel() {
        return recodeTargetVarLabel;
    }

    public void setRecodeTargetVarLabel(HtmlInputText vl) {
        this.recodeTargetVarLabel = vl;
    }
/*  deprecated (now using component-binding (recodeTargetVarLabel object)
    // value-binding 
    private String recodeVariableLabel;

    public void setRecodeVariableLabel(String vl) {
        recodeVariableLabel = vl;
    }

    public String getRecodeVariableLabel() {
        return recodeVariableLabel;
    }
*/
    // h:dataTable:recodeTable
    // @binding
    private UIData recodeTable = null;

    public UIData getRecodeTable() {
        return recodeTable;
    }

    public void setRecodeTable(UIData daTa) {
        this.recodeTable = daTa;
    }

    // @value
    private List<Object> recodeDataList = new ArrayList<Object>();

    public List<Object> getRecodeDataList() {
        return recodeDataList;
    }

    public void setRecodeDataList(List<Object> dl) {
        this.recodeDataList = dl;
    }

    // h:commandButton (Add Value/Range Button)
    // addValueRangeBttn@binding

    private HtmlCommandButton addValueRangeBttn = new HtmlCommandButton();

    public HtmlCommandButton getAddValueRangeBttn() {
        return addValueRangeBttn;
    }

    public void setAddValueRangeBttn(HtmlCommandButton hcb) {
        this.addValueRangeBttn = hcb;
    }

    // h:commandButton:addValueRangeBttn
    // @actionListener
    public void addValueRange(ActionEvent acev) {
        dbgLog.fine("before add: recodeDataList=" + recodeDataList);
        List<Object> rw = new ArrayList<Object>();
        // 0th: Drop
        rw.add(new Boolean(false));
        // 1st: New value
        rw.add("enter value here");
        // 2nd: New value label
        rw.add("enter label here");
        // conditions
        rw.add("enter a condition here");
        // 
        recodeDataList.add(rw);
        dbgLog.fine("after add: recodeDataList=" + recodeDataList);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(
            "recodeDataList", recodeDataList);
    }

    // A hash map that stores recoding schema
    private Map<String, List<Object>> recodeSchema = new HashMap<String, List<Object>>();

    // private Map<String, UIData> recodeSchema = new HashMap<String, UIData>();

    // public void setRecodeSchema(String varId, UIData rs){
    public void setRecodeSchema(String varId, List<Object> rs) {
        recodeSchema.put(varId, rs);
    }

    // public UIData getRecodeSchema (String varId){
    public List<Object> getRecodeSchema(String varId) {
        List<Object> rt = recodeSchema.get(varId);
        return rt;
    }

    // h:commandButton(Apply Recode Button)
    // recodeBttn@binding
    private HtmlCommandButton recodeButton = new HtmlCommandButton();

    public HtmlCommandButton getRecodeButton() {
        return recodeButton;
    }

    public void setRecodeButton(HtmlCommandButton hcb) {
        this.recodeButton = hcb;
    }

    /**
     * Saves the current recoding schema in the Map object recodeSchema
     * after an end-user clicked the apply-recode button.
     * Attached to the actionListener attribute of h:commandButton component
     * whose id is recodeBttn
     */
    public void saveRecodedVariable(ActionEvent acev) {
        dbgLog.fine("***** saveRecodedVariable(): begins here *****");

        // get the current var Id (base variable's ID)
        String oldVarId = getCurrentRecodeVariableId();
        dbgLog.fine("base var_id=" + oldVarId);        
        if (oldVarId == null){
            dbgLog.fine("source variable is not selected");
            msgSaveRecodeBttn.setRendered(true);
            msgSaveRecodeBttn.setText(
                "Select one of the existing variables as a source variable;<br />"+
                "and click the arrow button to set up the recode table"
                );
            return;
        }


        // get the varName in the input field
        String newVarName = (String) recodeTargetVarName.getValue();
        dbgLog.fine("new var name=" + newVarName);
        if (StringUtils.isEmpty(newVarName)){
            dbgLog.fine("Recode variable is not entered");
                msgSaveRecodeBttn.setRendered(true);
                msgSaveRecodeBttn.setText(
                "New variable name is not entered;<br />"+
                "enter a unique name in the variable-name box;");
                return;
        } else if (isRecodedVar(oldVarId)) {
            // replace (re-save) case
            dbgLog.fine("This variable id is found in the new variable set"
                + " and recoding scheme would be updated");
            dbgLog.fine("currentVarId=" + oldVarId);
            replaceRecodedVariable(oldVarId);
            return;
        } else {
            // newly create case
            if (isDuplicatedVariableName(newVarName)) {
                // duplicated name
                dbgLog.fine("The new variable name is already in use");
                msgSaveRecodeBttn.setRendered(true);
                msgSaveRecodeBttn.setText(
                    "The variable Name you entered is found "+
                    "among the existing variables;<br />"+
                    " enter a new variable name");
                return;
            } else {
                // sanity check against the name
                String whiteSpace = "\\s+";
                String prohibitedChars = "\\W+";
                String firstChar = "^[^a-zA-Z]";
                if (isVariableNameValid(newVarName, whiteSpace)) {
                    // whitespace found
                    msgSaveRecodeBttn.setRendered(true);
                    msgSaveRecodeBttn.setText(
                        "A whitespace character was found in "+
                        "the variable name;<br />"+
                        "Whitesapce characters are not allowed "+
                        "in a variable name.");
                    return;
                } else if (isVariableNameValid(newVarName, prohibitedChars)) {
                    // non-permissible character was found
                    msgSaveRecodeBttn.setRendered(true);
                    msgSaveRecodeBttn.setText(
                        "At least one non-permissible character was found "+
                        "in the variable name;<br />"+
                        "Use a-z, A-Z, _, 0-9 characters.");
                    return;
                } else if (isVariableNameValid(newVarName, firstChar)) {
                    // non-permissible character found
                    msgSaveRecodeBttn.setRendered(true);
                    msgSaveRecodeBttn.setText(
                        "The first character of a variable name must be "+
                        "an alphabet character.");
                    return;
                } else {
                    // unique and safe name
                    dbgLog.fine("The new variable name is unique");
                    msgSaveRecodeBttn.setRendered(false);
                }
            }
        }

        // get the varLabel in the input field
        String newVarLabel = (String) getRecodeTargetVarLabel().getValue();
        dbgLog.fine("new var Label=" + newVarLabel);

        // create a new var Id
        // new-case only

        StringBuilder sb = new StringBuilder(oldVarId + "_");
        sb.append(varIdGenerator(oldVarId));
        String newVarId = sb.toString();
        dbgLog.fine("newVarId=" + newVarId);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
            .put("currentRecodeVariableId", newVarId);

        // new-case only
        varCart.put(newVarId, newVarName);
        // new only
        getVarSetAdvStat().add(new Option(newVarId, newVarName));

        // add this new var to the old2new mapping table
        // new-case only
        if (baseVarToDerivedVar.containsKey(oldVarId)) {
            // already used for recoding
            Set<String> tmps = baseVarToDerivedVar.get(oldVarId);
            if (tmps.contains(newVarId)) {
                dbgLog.fine("This new var Id [" + newVarId
                    + "] is found in the set");
            } else {
                tmps.add(newVarId);
            }
        } else {
            // not-yet used for recoding
            Set<String> tmps = new HashSet<String>();
            dbgLog.fine("This new var Id [" + newVarId
                + "] is NOT found in the set");
            tmps.add(newVarId);
            baseVarToDerivedVar.put(oldVarId, tmps);
        }
        dbgLog.fine("old-2-new-var map=" + baseVarToDerivedVar);

        // remove a row whose condition cell is blank
        // both
        dbgLog.fine("start normalization");
        for (int i = (recodeDataList.size() - 1); i >= 0; i--) {
            List<Object> row = (List<Object>) recodeDataList.get(i);
            String raw = removeWhiteSpacesfromBothEnds((String) row.get(3));
            dbgLog.fine("after removing white spaces[=" + raw + "]");

            if (raw.equals("")) {
                recodeDataList.remove(i);
                dbgLog.fine("element[" + i + "] was removed");
            }
        }
        dbgLog.fine("end of normalization");

        dbgLog.fine("recodeDataList=" + recodeDataList);
        // saving the data for the recode-table
        // new and replace: both cases
        // replace-case: remove the key first?
        recodeSchema.put(newVarId, new ArrayList(recodeDataList));

        // update the value-label-mapping-data storage
        // new and replace: both cases
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
            .put("recodeSchema", recodeSchema);
        dbgLog.fine("recodeSchema=" + recodeSchema);

        if (!recodeVarNameSet.contains(newVarName)) {
            // 1st-time save

            // add this var to the mapping table
            // add this new var to the new2old mappting table
            derivedVarToBaseVar.put(newVarId, oldVarId);
            dbgLog.fine("new-2-old-var map=" + derivedVarToBaseVar);

            // add this var's name, label, id to the backing object
            // (recodedVarSet)
            List<Object> rw = new ArrayList<Object>();
            // [0]
            rw.add(new String(newVarName));
            // [1]
            rw.add(new String(newVarLabel));
            // [2]
            rw.add(newVarId);

            recodedVarSet.add(rw);
            // update recodeVarSet for the recodedVarTable
            FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("recodedVarSet", recodedVarSet);
            // add this newly created var to the set
            recodeVarNameSet.add(newVarName);

            // show the recode-var table
            pgRecodedVarTable.setRendered(true);
            dbgLog.fine("updated recodeVarNameSet:"+recodeVarNameSet);
        } else {
            // 2nd-time save
            // not required to add this var to mapping tables
            dbgLog.fine("newVarName="+newVarName);
            dbgLog.fine("existing recodeVarNameSet:"+recodeVarNameSet);
        }
        // show the recoded-var table
        // recodedVarTable.setRendered(true);
        dbgLog.fine("recodeVarSet=" + recodedVarSet);
        // save recode-source variable name for the header
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
            .put("currentRecodeVariableName", currentRecodeVariableName);

        dbgLog.fine("***** saveRecodedVariable(): ends here *****");
    }

    public String removeWhiteSpacesfromBothEnds(String src) {
        String s = src.replaceAll("\\s+$", "");
        return s.replaceAll("^\\s+", "");
    }

    public boolean isDuplicatedVariableName(String newVarName) {
        boolean rtvl = false;
        // against the set of the existing variable names
        for (Iterator el = dataVariables.iterator(); el.hasNext();) {
            DataVariable dv = (DataVariable) el.next();
            if (dv.getName().equals(newVarName)) {
                rtvl = true;
                break;
            }
        }
        return rtvl;
    }

    public boolean isVariableNameValid(String newVarName, String regex) {
        boolean rtvl = false;
        // 
        Pattern p = null;
        try {
            p = Pattern.compile(regex);
        } catch (PatternSyntaxException pex) {
            pex.printStackTrace();

        }
        Matcher matcher = p.matcher(newVarName);
        rtvl = matcher.find();
        return rtvl;
    }

    // Map from base-variable=>derived variable by varId
    // old(one) => new(many)
    private Map<String, Set<String>> baseVarToDerivedVar = new HashMap<String, Set<String>>();

    // new(one) => old(one)
    private Map<String, String> derivedVarToBaseVar = new HashMap<String, String>();

    // for duplication check
    private Set<String> recodeVarNameSet = new HashSet<String>();

    // newly derived variable's ID generator
    public String varIdGenerator(String varId) {
        int lstlen = 0;
        if (baseVarToDerivedVar.containsKey(varId)) {
            lstlen = baseVarToDerivedVar.get(varId).size();
        }
        return Integer.toString(lstlen);
    }

    // errormessage for recode-save button
    // ui:staticText
    private StaticText msgSaveRecodeBttn = new StaticText();

    public StaticText getMsgSaveRecodeBttn() {
        return msgSaveRecodeBttn;
    }

    public void setMsgSaveRecodeBttn(StaticText txt) {
        this.msgSaveRecodeBttn = txt;
    }

    public void resetMsgSaveRecodeBttn() {
        dbgLog.fine("***** within resetMsgSaveRecodeBttn *****");
        msgSaveRecodeBttn.setText(" ");
        msgSaveRecodeBttn.setRendered(false);
        
    }

    /**
     * remove a recoded Variable from the cart
     * 
     * ui:hyperlink attr: actionListener see recode block above
     */
    public void removeRecodedVariable(ActionEvent e) {
        dbgLog.fine("***** removeRecodedVariable(): begins here *****");

        // get data stored in the event row
        List<Object> tmpRecodeVarLine = 
            (List<Object>) getRecodedVarTable().getRowData();
        // get varId as a key of recodeSchema
        String newVarId = (String) tmpRecodeVarLine.get(2);
        String newVarName = (String) tmpRecodeVarLine.get(0);
        dbgLog.fine("recoded-var id=" + newVarId);
        // clear the error message if it still exists
        resetMsgVariableSelection();

        // remove this recoded var from the value-label storage Map
        // (recodeSchema)
        if (recodeSchema.containsKey(newVarId)) {
            recodeSchema.remove(newVarId);
        } else {
            dbgLog.fine("value-label table of this var [" + newVarId
                + "] is not found");
        }
        // remove this recoded var from the recoded-var table (recodedVarSet)

        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            String iter = (String) rvs.get(2);
            if (newVarId.equals(iter)) {
                dbgLog.fine("iter=" + i + " th element removed");
                recodedVarSet.remove(i);
                break;
            }
        }
        // remove this key from the old-2-new mapping table
        String oldVarId = derivedVarToBaseVar.get(newVarId);
        if (baseVarToDerivedVar.containsKey(oldVarId)) {

            Set<String> tmps = baseVarToDerivedVar.get(oldVarId);
            if (tmps.contains(newVarId)) {
                dbgLog.fine("This new var Id [" + newVarId
                    + "] is found in the set and to be removed");
                tmps.remove(newVarId);
                if (tmps.size() < 1) {
                    dbgLog.fine("There is no recoded var for this base-var(id="
                        + oldVarId + " name=" + getVariableNamefromId(oldVarId)
                        + ")");
                    baseVarToDerivedVar.remove(oldVarId);
                } else {
                    dbgLog.fine("The set is " + tmps.size()
                        + " for this base-var(id=" + oldVarId + " name="
                        + getVariableNamefromId(oldVarId) + ")");
                }
            } else {
                dbgLog.fine("This new var Id [" + newVarId
                    + "] is NOT found in the set");
            }

            derivedVarToBaseVar.remove(newVarId);
        } else {
            dbgLog.fine("recoded variable [" + newVarId
                + "] is not found in the new2old mapping table");
        }

        // remove this key from the new-2-old map
        if (derivedVarToBaseVar.containsKey(newVarId)) {
            derivedVarToBaseVar.remove(newVarId);
        } else {
            dbgLog.fine("recoded variable [" + newVarId
                + "] is not found in the new2old mapping table");
        }

        // if this variable is in the recode-table,
        String currentVarNameInBox = (String) recodeTargetVarName.getValue();
        dbgLog.fine("currentVarName in inputBox=" + currentVarNameInBox);
        if (newVarName.equals(currentVarNameInBox)) {
            dbgLog.fine("The variable in the recode table is"
                 + " the variable to be removed");
            // clear the table
            recodeDataList.clear();
            // reset the current variable Id
            setCurrentRecodeVariableName(null);
            setCurrentRecodeVariableId(null);

            FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("currentRecodeVariableId",
                    currentRecodeVariableId);

            FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("currentRecodeVariableName",
                    currentRecodeVariableName);

            // reset variable name and label
            recodeTargetVarName.resetValue();
            recodeTargetVarLabel.resetValue();
            // hide the recode table and add-row button
            groupPanelRecodeTableHelp.setRendered(false);
            recodeTable.setRendered(false);
            addValueRangeBttn.setRendered(false);

            varCart.remove(newVarId);
            // remove the existing option first
            removeOption(newVarId, getVarSetAdvStat());
        } else {
            dbgLog.fine("The variable in the recode table differs "
               + "from the variable to be removed");
        }

        // if no more recoded var, hide the recoded var table
        if (baseVarToDerivedVar.isEmpty()) {
            pgRecodedVarTable.setRendered(false);
        }
        // reset the recode-woring area
        groupPanelRecodeTableArea.setRendered(false);
        groupPanelRecodeInstruction2.setRendered(false);
        groupPanelRecodeInstruction1.setRendered(true);
            
        dbgLog.fine("***** removeRecodedVariable(): ends here *****");
    }

    public boolean isRecodedVar(String varId) {
        Pattern p = Pattern.compile("_");
        Matcher m = p.matcher(varId);
        return m.find();
    }

    public String getNewVarName(String newVarId) {
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            String iter = (String) rvs.get(2);
            if (newVarId.equals(iter)) {
                dbgLog.fine("recode data(name) found at " + i + " th row");
                return (String) rvs.get(0);
            }
        }
        return null;
    }

    public String getNewVarLabel(String newVarId) {
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            String iter = (String) rvs.get(2);
            if (newVarId.equals(iter)) {
                dbgLog.fine("recode data(label) found at " + i + " th row");
                return (String) rvs.get(1);
            }
        }
        return null;
    }

    public List<Object> getRecodedVarSetRow(String newVarId) {
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            String iter = (String) rvs.get(2);
            if (newVarId.equals(iter)) {
                dbgLog.fine("recode data(row) found at " + i + " th row");
                return rvs;
            }
        }
        return null;
    }

    // editRecodedVariable
    // ui:hyperlink @actionListener
    // se recode block above

    public void editRecodedVariable(ActionEvent e) {

        // get data stored in the event row
        List<Object> tmpRecodeVarLine = (List<Object>) getRecodedVarTable()
            .getRowData();
        dbgLog.fine("current recodedVar row:size=" + tmpRecodeVarLine.size());
        dbgLog.fine("current recodedVar row=" + tmpRecodeVarLine);
        // get varId
        String newVarId = (String) tmpRecodeVarLine.get(2);
        dbgLog.fine("recoded-var id=" + newVarId);

        dbgLog.fine("Is this a recoded var?[" + isRecodedVar(newVarId) + "]");
        // set this varId's list to the recodeTable

        if (recodeSchema.containsKey(newVarId)) {
            setRecodeDataList((List<Object>) recodeSchema.get(newVarId));

            recodeTable.setValue((List<Object>) recodeSchema.get(newVarId));
            // FacesContext.getCurrentInstance().getExternalContext()
            //      .getSessionMap().put("recodeDataList", recodeDataList);
                                                                           
            dbgLog.fine("contents of new value-label set="
                + (List<Object>) recodeSchema.get(newVarId));
            dbgLog.fine("contents of new value-label set=" + recodeDataList);
            clearRecodeTargetVarInfo();
            // update the current recode-Variable's ID
            String[] tmp = null;
            tmp = newVarId.split("_");
            dbgLog.fine("base-var Id from new var id=" + tmp[0]);
            dbgLog.fine("base-var Id from the map="
                + derivedVarToBaseVar.get(newVarId));
            // setCurrentRecodeVariableId(tmp[0]);

            currentRecodeVariableId = tmp[0];
            FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("currentRecodeVariableId",
                    currentRecodeVariableId);

            // update the varName
            // setRecodeVariableName( (String) tmpRecodeVarLine.get(0));

            // update the varLabel
            // setRecodeVariableLabel((String) tmpRecodeVarLine.get(1));

            recodeTargetVarName.setValue((String) tmpRecodeVarLine.get(0));
            recodeTargetVarLabel.setValue((String) tmpRecodeVarLine.get(1));

            // FacesContext.getCurrentInstance().getExternalContext()
            // .getSessionMap().put("recodeVariableName",recodeVariableName);
            // 
            // FacesContext.getCurrentInstance().getExternalContext()
            // .getSessionMap().put("recodeVariableLabel",recodeVariableLabel);
            //

            FacesContext.getCurrentInstance().renderResponse();

        } else {
            dbgLog.fine("value-label table of this var [" + newVarId
                + "] is not found");
        }

    }

    public void recallRecodedVariable(String newVarId) {
        dbgLog.fine("***** recallRecodedVariable(): begins here *****");

        // get data stored in a row of the recodedVarTable by Id
        List<Object> rvs = getRecodedVarSetRow(newVarId);
        if (rvs != null) {
            dbgLog.fine("requested newly recoded var was found");
            dbgLog.fine("new varName=" + rvs.get(0));
            dbgLog.fine("new varLabel=" + rvs.get(1));

            // set this varId's list to the recodeTable

            if (recodeSchema.containsKey(newVarId)) {
                setRecodeDataList((List<Object>) recodeSchema.get(newVarId));

                recodeTable.setValue((List<Object>) recodeSchema.get(newVarId));
                dbgLog.fine("contents of new value-label set="
                    + (List<Object>) recodeSchema.get(newVarId));
                dbgLog.fine("contents of new value-label set="
                    + recodeDataList);
                FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("recodeDataList", recodeDataList);

                clearRecodeTargetVarInfo();

                // update the current recode-Variable's ID
                String[] tmp = null;
                tmp = newVarId.split("_");
                dbgLog.fine("base-var Id from new var id=" + tmp[0]);
                dbgLog.fine("base-var Id from the map="
                    + derivedVarToBaseVar.get(newVarId));

                // currentRecodeVariableId=tmp[0];
                currentRecodeVariableId = newVarId;
                FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("currentRecodeVariableId",
                        currentRecodeVariableId);

                recodeTargetVarName.setValue((String) rvs.get(0));
                recodeTargetVarLabel.setValue((String) rvs.get(1));

                FacesContext.getCurrentInstance().renderResponse();

            } else {
                dbgLog.fine("value-label table of this var [" + newVarId
                    + "] is not found");
            }
        } else {
            dbgLog.warning("requested newly recoded var was not found="
                + newVarId);
        }
        dbgLog.fine("***** recallRecodedVariable(): ends here *****");

    }

    // updating the current recoding scheme
    public void replaceRecodedVariable(String newVarId) {

        dbgLog.fine("***** replaceRecodedVariable(): begins here *****");
        dbgLog.fine("current var id (from: args)=" + newVarId);
        dbgLog.fine("current var id (from: method)="
            + getCurrentRecodeVariableId());

        // get the latest varName in the input field
        String newVarName = (String) recodeTargetVarName.getValue();
        dbgLog.fine("new var Name =" + newVarName);

        // sanity check against the name
        String whiteSpace = "\\s+";
        String prohibitedChars = "\\W+";
        String firstChar = "^[^a-zA-Z]";
        if (isVariableNameValid(newVarName, whiteSpace)) {
            // whitespace found
            msgSaveRecodeBttn.setRendered(true);
            msgSaveRecodeBttn
                .setText("A whitespace character was found in the variable name;<br />Whitesapce characters are not allowed in a variable name.");
            return;
        } else if (isVariableNameValid(newVarName, prohibitedChars)) {
            // non-permissible character found
            msgSaveRecodeBttn.setRendered(true);
            msgSaveRecodeBttn
                .setText("At least one non-permissible character was found in the variable name;<br />Use a-z, A-Z, _, 0-9 characters.");
            return;
        } else if (isVariableNameValid(newVarName, firstChar)) {
            // non-permissible character found
            msgSaveRecodeBttn.setRendered(true);
            msgSaveRecodeBttn
                .setText("The first character of a variable name must be an alphabet character.");
            return;
        } else {
            // unique and safe name
            dbgLog.fine("The new variable name is unique");
            msgSaveRecodeBttn.setRendered(false);
            // msgSaveRecodeBttn.setText("The variable name is unique");
        }

        // get the latest varLabel in the input field
        String newVarLabel = (String) getRecodeTargetVarLabel().getValue();
        dbgLog.fine("new var Label=" + newVarLabel);

        // create a new var Id
        // new-case only
        dbgLog.fine("newVarId=" + newVarId);

        // replace-case only
        // remove the current varId-varName pair: variable name might have been
        // updated
        varCart.remove(newVarId);

        // new and replace: both cases
        varCart.put(newVarId, newVarName);

        // replace only: remove the existing option first
        removeOption(newVarId, getVarSetAdvStat());
        // new and replace: both cases
        getVarSetAdvStat().add(new Option(newVarId, newVarName));

        // add this new var to the old2new mapping table
        // new-case only
        dbgLog.fine("old-2-new-var map=" + baseVarToDerivedVar);

        // remove a row whose condition cell is blank
        // both
        dbgLog.fine("start normalization");
        for (int i = (recodeDataList.size() - 1); i >= 0; i--) {
            List<Object> row = (List<Object>) recodeDataList.get(i);
            String raw = removeWhiteSpacesfromBothEnds((String) row.get(3));
            dbgLog.fine("after removing white spaces[=" + raw + "]");

            if (raw.equals("")) {
                recodeDataList.remove(i);
                dbgLog.fine("element[" + i + "] was removed");
            }
        }
        dbgLog.fine("end of normalization");

        dbgLog.fine("recodeDataList=" + recodeDataList);
        // saving the data for the recode-table
        // replace-case: remove the existing entry first
        recodeSchema.remove(newVarId);

        // new and replace: both cases
        recodeSchema.put(newVarId, new ArrayList(recodeDataList));

        // update the value-label-mapping-data storage
        // new and replace: both cases
        FacesContext.getCurrentInstance().getExternalContext()
            .getSessionMap().put("recodeSchema", recodeSchema);
        dbgLog.fine("recodeSchema=" + recodeSchema);

        // replace case
        // 2nd-time save
        // update the existing variable name and label because they might have
        // been modified
        dbgLog.fine("new-2-old-var map=" + derivedVarToBaseVar);

        // get the current row of this recoded var
        int location = -1;
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> row = (List<Object>) recodedVarSet.get(i);
            String varId = (String) row.get(2);
            dbgLog.fine(i + "-th current varId=" + varId);
            if (varId.equals(newVarId)) {
                dbgLog.fine("The ID was found in the " + i + "-th row");
                location = i;
            }
        }
        if (location >= 0) {
            List<Object> oldrw = (List<Object>) recodedVarSet.get(location);

            boolean isVariableNameUpdated = false;
            String oldVarName = (String) oldrw.get(0);
            if (oldVarName.equals(newVarName)) {
                dbgLog.fine("The variable name was not updated");
            } else {
                dbgLog.fine("The old variable name(" + oldVarName
                    + ") is replaced by " + newVarName);

                // The variable name has been updated;
                // remove the current name from recodeVarNameSet first
                if (recodeVarNameSet.remove(oldVarName)) {
                    dbgLog.fine("The old variable name was successfully removed");
                } else {
                    dbgLog.fine("The old variable name(" + oldVarName
                        + ") was not removed");
                }
                isVariableNameUpdated = true;
            }
            // remove the current row first
            recodedVarSet.remove(location);

            // add the new row
            // add this var's name, label, id to the backing object
            // (recodedVarSet)
            List<Object> rw = new ArrayList<Object>();
            // [0]
            rw.add(new String(newVarName));
            // [1]
            rw.add(new String(newVarLabel));
            // [2]
            rw.add(newVarId);
            recodedVarSet.add(rw);
            // update recodeVarSet for the recodedVarTable
            FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("recodedVarSet", recodedVarSet);

            // if the variable name has been updated,
            // remove the current name from recodeVarNameSet and
            // add the new one to it

            if (isVariableNameUpdated) {
                recodeVarNameSet.add(newVarName);
            }
        } else {
            dbgLog.warning("This variable was not saved(id=" + newVarId + ")");
        }
        // show the recoded-var table
        // recodedVarTable.setRendered(true);
        dbgLog.fine("recodeVarSet(after update)=" + recodedVarSet);
        dbgLog.fine("***** replaceRecodedVariable(): ends here *****");
    }


    public Map<String, List<String>> getRecodedVarParameters() {
        Map<String, List<String>> mpl = new HashMap<String, List<String>>();
        // new var name list
        List<String> vns = new ArrayList<String>();
        // Set<String> vids = recodeSchema.keySet();
        for (Object rw : recodedVarSet) {
            // add variable label
            List<Object> rwi = (List<Object>) rw;
            // [0] varName
            // [1] varLabel
            // [2] varId
            String newVarName = (String) rwi.get(0);
            String newVarLabel = (String) rwi.get(1);
            String newVarId = (String) rwi.get(2);
            // add newVarId to newVarName
            mpl.put("v" + newVarId, Arrays.asList(newVarName));
            dbgLog.fine("Id-Name: " + newVarId + "=>" + newVarName);
            // add new varName
            vns.add(newVarName);
            mpl.put("ud_" + newVarName + "_q_lebaLrav", Arrays
                .asList(newVarLabel));
            // add variable type:  0 for character var
            mpl.put("ud_" + newVarName + "_q_epyTrav", Arrays.asList("1")); 
            // add value-label-condition
            List<Object> rdtbl = (List<Object>) recodeSchema.get(newVarId);
            dbgLog.fine("rdtbl=" + rdtbl);
            List<String> delList = new ArrayList<String>();
            for (Object rdtblrw : rdtbl) {
                List<Object> rdtbli = (List<Object>) rdtblrw;
                String baseVarName = getVariableNamefromId(derivedVarToBaseVar
                    .get(newVarId));
                dbgLog.fine("rdtbli=" + rdtbli);

                if ((Boolean) rdtbli.get(0)) {
                    // delete flag: boolean true
                    // delete value
                    // "ud_"+ {newVarName} + "_q_eteleD"
                    // "eteleD|"+{VarName}+"|"+{condition}
                    dbgLog.fine("delete this value=" + rdtbli.get(3));
                    delList.add("eteleD|" + baseVarName + "|" + rdtbli.get(3));

                } else {
                    // delete flag: boolean false, i.e.,
                    // value - label - condition
                    // "ud_"+ {newVarName} + "_q_" +{value}
                    // {label}+"|"+{VarName}+"|"+{condition}
                    dbgLog.fine("keep this value=" + rdtbli.get(3));
                    String pmky = "ud_" + newVarName + "_q_" + rdtbli.get(1);
                    dbgLog.fine("pmky=" + pmky);
                    if (mpl.containsKey(pmky)) {
                        // key exits
                        List<String> tmpvl = (List<String>) mpl.get(pmky);
                        dbgLog.fine("tmpvl:b=" + tmpvl);
                        String pmvl = rdtbli.get(2) + "|" + baseVarName + "|"
                            + rdtbli.get(3);
                        dbgLog.fine("pmvl=" + pmvl);
                        tmpvl.add(pmvl);
                        dbgLog.fine("tmpvl:a=" + tmpvl);
                        // mpl.put(pmky, new ArrayList(tmpvl) );
                        mpl.put(pmky, tmpvl);

                    } else {
                        List<String> pvlst = new ArrayList();
                        pvlst.add(rdtbli.get(2) + "|" + baseVarName + "|"
                            + rdtbli.get(3));
                        mpl.put(pmky, pvlst);
                    }
                    // mpl.put("ud_"+newVarName+"_q_"+rdtbli.get(1),
                    // Arrays.asList(rdtbli.get(2)+"|"+baseVarName+"|"+rdtbli.get(3))
                    // );
                }

            } // for:inner

            if (delList.size() > 0) {
                mpl.put("ud_" + newVarName + "_q_eteleD", delList);
            }

        } // for:outer
        // add newVarNameSet
        mpl.put("newVarNameSet", vns);

        return mpl;
    }


    // -----------------------------------------------------------------------
    // recoded-variable Table section
    // -----------------------------------------------------------------------
    // / title and message line

    // h:HtmlOutputText: recodedVarTableTitle@binding
    private HtmlOutputText recodedVarTableTitle = new HtmlOutputText();

    public HtmlOutputText getRecodedVarTableTitle() {
        return recodedVarTableTitle;
    }

    public void setRecodedVarTableTitle(HtmlOutputText hot) {
        this.recodedVarTableTitle = hot;
    }

    // h:panelGroup PGrecodedVarTable
    // @binding
    private HtmlPanelGroup pgRecodedVarTable = new HtmlPanelGroup();

    public HtmlPanelGroup getPgRecodedVarTable() {
        return pgRecodedVarTable;
    }

    public void setPgRecodedVarTable(HtmlPanelGroup pg) {
        this.pgRecodedVarTable = pg;
    }

    // h:dataTable: recodedVarTable
    // @binding
    private UIData recodedVarTable = null;

    public UIData getRecodedVarTable() {
        return recodedVarTable;
    }

    public void setRecodedVarTable(UIData data) {
        this.recodedVarTable = data;
    }

    // @value
    public List<Object> recodedVarSet = new ArrayList<Object>();

    public void setRecodedVarSet(List<Object> dt) {
        this.recodedVarSet = dt;
    }

    public List<Object> getRecodedVarSet() {
        return recodedVarSet;
    }

    public void hideRecodeTableArea() {
        groupPanelRecodeTableHelp.setRendered(false);
        recodeTable.setRendered(false);
        addValueRangeBttn.setRendered(false);
    }

    // recode section ends here
    // </editor-fold>

    // -----------------------------------------------------------------------
    // eda section
    // -----------------------------------------------------------------------
    // <editor-fold desc="EDA">

    // LHS: list box related fields

    private HelpInline helpInline3 = new HelpInline();

    public HelpInline getHelpInline3() {
        return helpInline3;
    }

    public void setHelpInline3(HelpInline helpInline3) {
        this.helpInline3 = helpInline3;
    }

    // RHS: checkbox area related fields
    // analysis:h:selectManyCheckbox@binding
    private HtmlSelectManyCheckbox edaOptionSet = new HtmlSelectManyCheckbox();

    public HtmlSelectManyCheckbox getEdaOptionSet() {
        return edaOptionSet;
    }

    public void setEdaOptionSet(HtmlSelectManyCheckbox edaOptionSet) {
        this.edaOptionSet = edaOptionSet;
    }

    // edaOptionNumeric:f:selectItem@itemValue
    // edaOptionGraphic:f:selectItem@itemValue
    private List edaOptionItems = null;

    public List getEdaOptionItems() {
        if (edaOptionItems == null) {
            edaOptionItems = new ArrayList();
            edaOptionItems.add("A01");
            edaOptionItems.add("A02");
        }
        return edaOptionItems;
    }

    // submit button
    // edaBttn:h:commandButton@binding
    private HtmlCommandButton edaButton = new HtmlCommandButton();

    public HtmlCommandButton getEdaButton() {
        return edaButton;
    }

    public void setEdaButton(HtmlCommandButton hcb) {
        this.edaButton = hcb;
    }

    // checking parameters before submission-
    public boolean checkEdaParameters() {
        boolean result = true;

        Object[] vs = edaOptionSet.getSelectedValues();
        // param-checking conditions
        if (vs.length < 1) {
            dbgLog.fine("EDA(checkEdaParameters()): no option is checked");
            result = false;
        } else {
            dbgLog.fine("EDA(checkEdaParameters): number of selected options="
                + vs.length);
        }
        return result;
    }


    // msgEdaButton:ui:StaticText@binding
    private StaticText msgEdaButton = new StaticText();

    public StaticText getMsgEdaButton() {
        return msgEdaButton;
    }

    public void setMsgEdaButton(StaticText txt) {
        this.msgEdaButton = txt;
    }

    private String msgEdaButtonTxt;

    public String getMsgEdaButtonTxt() {
        return msgEdaButtonTxt;
    }

    public void setMsgEdaButtonTxt(String txt) {
        this.msgEdaButtonTxt = txt;
    }

    public void resetMsgEdaButton() {
        dbgLog.fine("***** within resetMsgEdaButton *****");
        msgEdaButton.setText(" ");
        FacesContext.getCurrentInstance().getExternalContext()
            .getSessionMap().put("msgEdaButtonTxt", msgEdaButtonTxt);
        msgEdaButton.setVisible(false);

        dbgLog.fine("***** resetMsgEdaButton: end  *****");
    }

    // end of eda section ----------------------------------------------------
    // </editor-fold>

    // edaBttn:h:commandButton@action
    public String edaAction() {
        
        dbgLog.fine("selected tab(eda)="+getTabSet1().getSelected());
        // clear the error message around the EDA button if they exisit
        resetMsgEdaButton();
        
        if (checkEdaParameters()) {

            FacesContext cntxt = FacesContext.getCurrentInstance();

            HttpServletResponse res = (HttpServletResponse) cntxt
                .getExternalContext().getResponse();
            HttpServletRequest req = (HttpServletRequest) cntxt
                .getExternalContext().getRequest();

                dbgLog.fine("***** within edaAction() *****");

                StudyFile sf = dataTable.getStudyFile();
                Long noRecords = dataTable.getRecordsPerCase();

                String dsbUrl = getDsbUrl();
                dbgLog.fine("dsbUrl=" + dsbUrl);

                String serverPrefix = req.getScheme() + "://"
                + req.getServerName() + ":" + req.getServerPort()
                + req.getContextPath();
                /*
                String serverPrefix = "http://dvn-alpha.hmdc.harvard.edu"
                    + req.getContextPath();
                */    
                dbgLog.fine("serverPrefix"+serverPrefix);

                /*
                 * "optnlst_a" => "A01|A02|A03",
                 * "analysis" => "A01 A02",
                 * "varbl" => "v1.3 v1.10 v1.13 v1.22 v1.40", 
                 * "charVarNoSet" =>
                 * "v1.10|v1.719",
                 */

                Map<String, List<String>> mpl = new HashMap<String, List<String>>();
                
                Object[] vs = edaOptionSet.getSelectedValues();
                // analysis=[A01, A02]
                List<String> alst = new ArrayList<String>();

                for (int i = 0; i < vs.length; i++) {
                    dbgLog.fine("eda option[" + i + "]=" + vs[i]);
                    alst.add((String) vs[i]);
                }

                mpl.put("analysis", alst);

                mpl.put("optnlst_a", Arrays.asList("A01|A02|A03"));

                // if there is a user-defined (recoded) variables
                /*
                if (recodedVarSet.size() > 0) {
                    mpl.putAll(getRecodedVarParameters());
                }
                */
//                dbgLog.fine("citation info to be sent:\n" + citation);
//                mpl.put("OfflineCitation", Arrays.asList(citation));
//                mpl.put("appSERVER", Arrays.asList(req.getServerName() + ":"
//                    + req.getServerPort() + req.getContextPath()));
                
                mpl.put("studytitle", Arrays.asList(studyTitle));
                mpl.put("studyno", Arrays.asList(studyId.toString()));
                mpl.put("studyURL", Arrays.asList(studyURL));
                mpl.put("browserType", Arrays.asList(browserType));
                
                mpl.put("recodedVarIdSet", getRecodedVarIdSet());
                mpl.put("recodedVarNameSet",getRecodedVarNameSet());
                mpl.put("recodedVarLabelSet", getRecodedVarLabelSet());
                mpl.put("recodedVarTypeSet", getRecodedVariableType());
                mpl.put("recodedVarBaseTypeSet", getBaseVariableTypeForRecodedVariable());
                
                mpl.put("baseVarIdSet",getBaseVarIdSetFromRecodedVarIdSet());
                mpl.put("baseVarNameSet",getBaseVarNameSetFromRecodedVarIdSet());
                
                mpl.put("requestType", Arrays.asList("EDA"));

                
            // -----------------------------------------------------
            // New processing route
            // 
            // Step 0. Locate the data file and its attributes
    
            String fileId = sf.getId().toString();
            //String fileURL = serverPrefix + "/FileDownload/?fileId=" + fileId + "&isSSR=1&xff=0&noVarHeader=1";
            //String fileURL = "http://dvn-alpha.hmdc.harvard.edu" + "/dvn/FileDownload/?fileId=" + fileId + "&isSSR=1&xff=0&noVarHeader=1";
            String fileURL = "http://localhost:" + req.getServerPort() + "/dvn" + "/FileDownload/?fileId=" + fileId + "&isSSR=1&xff=0&noVarHeader=1";
            
            dbgLog.fine("fileURL="+fileURL);
            
            String fileloc = sf.getFileSystemLocation();
            String tabflnm = sf.getFileName();
            boolean sbstOK = sf.isSubsettable();
            String flct = sf.getFileType();
            
            dbgLog.fine("location="+fileloc);
            dbgLog.fine("filename="+tabflnm);
            dbgLog.fine("subsettable="+sbstOK);
            dbgLog.fine("filetype="+flct);

            DvnRJobRequest sro = null;

            List<File> zipFileList = new ArrayList();

            // the data file for downloading/statistical analyses must be subset-ready
            // local (relative to the application) file case 
            // note: a typical remote case is: US Census Bureau
            File tmpsbfl = null;
            if (sbstOK){
                
                try {

                // Step 1. temporarily store the whole data set in a temp directory

                    // Create a URL for the data file
                    URL url = new URL(fileURL);

                    // temp data file that stores incoming data from the above URL
                    File tmpfl = File.createTempFile("tempTabfile.", ".tab");
                    deleteTempFileList.add(tmpfl);
                    // temp subset file that stores requested variables 
                    tmpsbfl = File.createTempFile("tempsubsetfile.", ".tab");
                    deleteTempFileList.add(tmpsbfl);
                    //zipFileList.add(tmpsbfl);

                    // Typical file-copy idiom 
                    // incoming/outgoing streams
                    InputStream inb = new BufferedInputStream(url.openStream());
                    OutputStream outb = new BufferedOutputStream(new FileOutputStream(tmpfl));

                    int bufsize;
                    byte [] bffr = new byte[8192];
                    while ((bufsize = inb.read(bffr))!=-1) {
                        outb.write(bffr, 0, bufsize);
                    }
                    
                    inb.close();
                    outb.close();
                    
                    // Checks the obtained data file
                    if (tmpfl.exists()){
                        Long wholeFileSize = tmpfl.length();
                        dbgLog.fine("whole file:length="+wholeFileSize);
                        dbgLog.fine("tmp file:name="+tmpfl.getAbsolutePath());
                        
                        if (wholeFileSize <= 0){
                            // subset file exists but it is empty
                        
                            msgEdaButton.setText("* an data file is empty");
                            msgEdaButton.setVisible(true);
                            dbgLog.warning("exiting edaAction() due to a file access error:"+
                            "a data file is empty"
                            );
                            getVDCRequestBean().setSelectedTab("tabEda");

                            return "failure";
                        }
                       
                    } else {
                        // file was not created/downloaded
                        msgEdaButton.setText("* a data file was not created");
                        msgEdaButton.setVisible(true);
                        dbgLog.warning("exiting edaAction() due to a file access error:"+
                        "a data file was not created"
                        );
                        getVDCRequestBean().setSelectedTab("tabEda");

                        return "failure";
                    }

                    // source data file: full-path name
                    String cutOp1 = tmpfl.getAbsolutePath();

                    // result(subset) data file: full-path name
                    String cutOp2 = tmpsbfl.getAbsolutePath();

                    // check whether a source file is tab-delimited or not

                    boolean fieldcut = true;
                    if ((noRecords != null) && (noRecords >=1)){
                        fieldcut = false;
                    }
                    
                    if (fieldcut){
                // Step 2.a. Set-up parameters for subsetting: cutting requested fields of data
                        // from a temp (whole) delimited file

                        Set<Integer> fields = getFieldNumbersForSubsetting();
                         dbgLog.fine("fields="+fields);

                        // Create an instance of DvnJavaFieldCutter
                        FieldCutter fc = new DvnJavaFieldCutter();

                        // Executes the subsetting request
                        fc.subsetFile(cutOp1, cutOp2, fields);

                    } else {
                // Step 2.b. Set-up parameters for subsetting: cutting requested columns of data
                        // from a temp (whole) non-delimited file
                        // Using new, native implementation of fixed-field cutting 
                        // (instead of executing rcut in a shell)

                        Map<Long, List<List<Integer>>> varMetaSet = getSubsettingMetaData(); 
                        DvnNewJavaFieldCutter fc = new DvnNewJavaFieldCutter(varMetaSet);

                        try {
                            fc.cutColumns(new File(cutOp1), varMetaSet.size(), 0, "\t", cutOp2);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();

                            msgEdaButton.setText("* could not generate subset due to an IO problem");
                            msgEdaButton.setVisible(true); 
                            dbgLog.warning("exiting edaAction() due to an IO problem ");
                            getVDCRequestBean().setSelectedTab("tabEda");

                            return "failure";
                        } catch (RuntimeException re){
                            re.printStackTrace();
                            
                            msgEdaButton.setText("* could not generate subset due to an runtime error");
                            msgEdaButton.setVisible(true); 
                            dbgLog.warning("exiting edaAction() due to an runtime error");
                            getVDCRequestBean().setSelectedTab("tabEda");
                        }
                        // end: non-delimited case
                    }
                    
                    // Checks the resulting subset file 
                    if (tmpsbfl.exists()){
                        Long subsetFileSize = tmpsbfl.length();
                        dbgLog.fine("subsettFile:Length="+subsetFileSize);
                        dbgLog.fine("tmpsb file name="+tmpsbfl.getAbsolutePath());
                        
                        if (subsetFileSize > 0){
                            mpl.put("subsetFileName", Arrays.asList(cutOp2));
                            mpl.put("subsetDataFileName",Arrays.asList(tmpsbfl.getName()));
                        } else {
                            // subset file exists but it is empty
                        
                            msgEdaButton.setText("* an subset file is empty");
                            msgEdaButton.setVisible(true);
                            dbgLog.warning("exiting edaAction() due to a subsetting error:"+
                            "a subset file is empty"
                            );
                            getVDCRequestBean().setSelectedTab("tabEda");

                            return "failure";

                        }
                    } else {
                        // subset file was not created
                        msgEdaButton.setText("* a subset file was not created");
                        msgEdaButton.setVisible(true);
                        dbgLog.warning("exiting edaAction() due to a subsetting error:"+
                        "a subset file was not created"
                        );
                        getVDCRequestBean().setSelectedTab("tabEda");

                        return "failure";

                    }
                    
                // Step 3. Organizes parameters/metadata to be sent to the implemented
                    // data-analysis-service class
                    
                    //Map<String, Map<String, String>> vls = getValueTableForRequestedVariables(getDataVariableForRequest());
                    Map<String, Map<String, String>> vls = getValueTablesForAllRequestedVariables();
                    
                    sro = new DvnRJobRequest(getDataVariableForRequest(), mpl, vls, recodeSchema);

                    dbgLog.fine("sro dump:\n"+ToStringBuilder.reflectionToString(sro, ToStringStyle.MULTI_LINE_STYLE));
                    
                // Step 4. Creates an instance of the the implemented 
                    // data-analysis-service class 

                    DvnRDataAnalysisServiceImpl das = new DvnRDataAnalysisServiceImpl();

                    // Executes a request of downloading or data analysis and 
                    // capture result info as a Map <String, String>

                    resultInfo = das.execute(sro);
                    
                // Step 5. Checks the DSB-exit-status code
                    if (resultInfo.get("RexecError").equals("true")){
                        msgEdaButton.setText("* The Request failed due to an R-runtime error");
                        msgEdaButton.setVisible(true);
                        dbgLog.fine("exiting edaAction() due to an R-runtime error");
                        getVDCRequestBean().setSelectedTab("tabEda");

                        return "failure";
                    } else {
                        if (recodeSchema.size()> 0){
                            resultInfo.put("subsettingCriteria",sro.getSubsetConditionsForCitation());
                        } else {
                            resultInfo.put("subsettingCriteria","");
                        }
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();

                    msgEdaButton.setText("* file URL is malformed");
                    msgEdaButton.setVisible(true);
                    dbgLog.warning("exiting edaAction() due to a URL problem ");
                    getVDCRequestBean().setSelectedTab("tabEda");

                    return "failure";
                    
                } catch (IOException e) {
                    // this may occur if the dataverse is not released
                    // the file exists, but it is not accessible 
                    e.printStackTrace();
                    
                    msgEdaButton.setText("* an IO problem occurred");
                    msgEdaButton.setVisible(true);
                    dbgLog.warning("exiting edaAction() due to an IO problem ");
                    getVDCRequestBean().setSelectedTab("tabEda");

                    return "failure";
                }
                
                // end of the subset-OK case
            } else {
                // not subsettable data file
                msgEdaButton.setText("* this data file is not subsettable file");
                msgEdaButton.setVisible(true);
                dbgLog.warning("exiting edaAction(): the data file is not subsettable ");
                getVDCRequestBean().setSelectedTab("tabEda");

                return "failure";

            } // end:subsetNotOKcase

            // final processing steps for all successful cases

            resultInfo.put("offlineCitation", citation);
            resultInfo.put("studyTitle", studyTitle);
            resultInfo.put("studyNo", studyId.toString());
            resultInfo.put("studyURL", studyURL);
            resultInfo.put("R_min_verion_no","2.7.0");
            resultInfo.put("dataverse_version_no","1.3");
            
            dbgLog.fine("RwrkspFileName="+resultInfo.get("wrkspFileName"));

            // writing necessary files
            try{
            
            
                // rename the subsetting file
                File tmpsbflnew = File.createTempFile(SUBSET_FILENAME_PREFIX + resultInfo.get("PID") +".", ".tab");
                deleteTempFileList.add(tmpsbflnew);
                InputStream inb = new BufferedInputStream(new FileInputStream(tmpsbfl));
                OutputStream outb = new BufferedOutputStream(new FileOutputStream(tmpsbflnew));

                int bufsize;
                byte [] bffr = new byte[8192];
                while ((bufsize = inb.read(bffr))!=-1) {
                    outb.write(bffr, 0, bufsize);
                }
                inb.close();
                outb.close();
                
                String rhistNew = StringUtils.replace(resultInfo.get("RCommandHistory"), tmpsbfl.getName(),tmpsbflnew.getName());
                
                
                //zipFileList.add(tmpsbflnew);

                // (1) write a citation file 
//                String citationFilePrefix = "citationFile_"+ resultInfo.get("PID") + "_";
//                File tmpcfl = File.createTempFile(citationFilePrefix, ".txt");
//
//                zipFileList.add(tmpcfl);
//                deleteTempFileList.add(tmpcfl);
//
//                DvnCitationFileWriter dcfw = new DvnCitationFileWriter(resultInfo);
//
//                String fmpcflFullname = tmpcfl.getAbsolutePath();
//                String fmpcflname = tmpcfl.getName();
//                dcfw.write(tmpcfl);

                // (2) R command file
                String rhistoryFilePrefix = "RcommandFile_" + resultInfo.get("PID") + "_";
                File tmpRhfl = File.createTempFile(rhistoryFilePrefix, ".R");

                zipFileList.add(tmpRhfl);
                deleteTempFileList.add(tmpRhfl);
                resultInfo.put("dvn_R_helper_file","dvn_helper.R");
                DvnReplicationCodeFileWriter rcfw = new DvnReplicationCodeFileWriter(resultInfo);
                rcfw.writeEdaCode(tmpRhfl);
                

                // (3)RData Replication file
                String wrkspFileName = resultInfo.get("wrkspFileName");
                dbgLog.fine("wrkspFileName="+wrkspFileName);
                
                File RwrkspFileName = new File(wrkspFileName);
                if (RwrkspFileName.exists()){
                    dbgLog.fine("RwrkspFileName:length="+RwrkspFileName.length());

                    zipFileList.add(RwrkspFileName);

                } else {
                    dbgLog.fine("RwrkspFileName does not exist");
                    //msgEdaButton.setText("* The workspace file is not available");
                    //msgEdaButton.setVisible(true);
                    dbgLog.warning("edaAction(): R workspace file was not transferred");
                    //getVDCRequestBean().setSelectedTab("tabEda");

                    //return "failure";
                }
                deleteTempFileList.add(RwrkspFileName);
                
//                // vdc_startup.R file
//                String vdc_startupFileName = resultInfo.get("vdc_startupFileName");
//                dbgLog.fine("vdc_startupFileName="+vdc_startupFileName);
//                File vdcstrtFileName = new File(vdc_startupFileName);
//                if (vdcstrtFileName.exists()){
//                    dbgLog.fine("vdcstrtFileName:length="+vdcstrtFileName.length());
//                    zipFileList.add(vdcstrtFileName);
//                } else {
//                    dbgLog.fine("vdcstrtFileName does not exist");
//                    //msgEdaButton.setText("* vdc_startup.R is not available");
//                    //msgEdaButton.setVisible(true);
//                    dbgLog.warning("edaAction(): vdc_startup.R was not transferred");
//                    //getVDCRequestBean().setSelectedTab("tabEda");
//
//                    //return "failure";
//                }
//                deleteTempFileList.add(vdcstrtFileName);
                // add replication readme file
                // (4) readme file
               // zipFileList.add(REP_README_FILE);
               
                File readMeFile = File.createTempFile(REP_README_FILE_PREFIX + resultInfo.get("PID") +"_", ".txt");
                DvnReplicationREADMEFileWriter rw = new DvnReplicationREADMEFileWriter(resultInfo);
                rw.writeREADMEfile(readMeFile);
                //zipFileList.add(REP_README_FILE);
                zipFileList.add(readMeFile);
                deleteTempFileList.add(readMeFile);
               
                // (5) css file
                zipFileList.add(DVN_R2HTML_CSS_FILE);
                // (6) dvn_R_helper
                zipFileList.add(DVN_R_HELPER_FILE);
                // zip the following files as a replication-pack
                //
                // local     local        local      remote
                // citation, tab-file,   R history,  wrksp

                for (File f : zipFileList){
                    dbgLog.fine("path="+f.getAbsolutePath() +"\tname="+ f.getName());
                }

                // zipping all required files
                try{
                    String zipFilePrefix = "zipFile_" + resultInfo.get("PID") + "_";
                    File zipFile  = File.createTempFile(zipFilePrefix, ".zip");
                    
                    //res.setContentType("application/zip");
                    String zfname = zipFile.getAbsolutePath();
                    //res.setHeader("content-disposition", "attachment; filename=" + zfname);
                    
                    OutputStream zfout = new FileOutputStream(zipFile);
                    //zipFiles(res.getOutputStream(), zipFileList);
                    zipFiles(zfout, zipFileList);
                    deleteTempFileList.add(zipFile);
                    if (zipFile.exists()){
                        Long zipFileSize = zipFile.length();
                        dbgLog.fine("zip file:length="+zipFileSize);
                        dbgLog.fine("zip file:name="+zipFile.getAbsolutePath());
                        if (zipFileSize > 0){
                            resultInfo.put("replicationZipFile", zfname);
                            resultInfo.put("replicationZipFileName", zipFile.getName());
                        } else {
                            dbgLog.fine("zip file is empty");
                        }
                    } else {
                        dbgLog.fine("zip file was not saved");
                    }
                    
                    resultInfo.remove("RCommandHistory");

                    // put resultInfo into the session object

                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(
                        "resultInfo", resultInfo);
                    
                    dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                    
                    dbgLog.fine("***** within edaAction(): succcessfully ends here *****");

                        
                    return "success";
                
                } catch (IOException e){
                    // file-access problem, etc.
                    e.printStackTrace();
                    dbgLog.fine("zipping IO exception");
                    msgEdaButton.setText("* an IO problem occurred during zipping replication files");
                    msgEdaButton.setVisible(true);
                    dbgLog.warning("exiting edaAction() due to an zipping IO problem ");
                    //getVDCRequestBean().setSelectedTab("tabEda");
                    dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                    return "success";
                }
                // end of zipping step

            } catch (IOException e){
                // io errors caught during writing files
                e.printStackTrace();
                
                msgEdaButton.setText("* an IO problem occurred");
                msgEdaButton.setVisible(true);
                dbgLog.warning("exiting edaAction() due to an IO problem ");
                getVDCRequestBean().setSelectedTab("tabEda");

                return "failure";
            }







            
            // end of CheckParameters: OK case
        } else {
            // parameters are not complete: show error message;
            msgEdaButton.setText("* Select at least one option");
            msgEdaButton.setVisible(true);
            dbgLog.warning("exiting edaAction(): selection is incomplete");
            getVDCRequestBean().setSelectedTab("tabEda");

            return "failure";
        }

    }
    
    // -----------------------------------------------------------------------
    // AdvStat section
    // -----------------------------------------------------------------------
    // <editor-fold desc="Advanced Statistics">
    // <editor-fold desc="Adv Stat box and panels">
    // Selected variable box

    // @value: options for dropDown menu
    private List<Option> modelMenuOptions = new ArrayList<Option>();

    public List<Option> getModelMenuOptions() {
        return this.modelMenuOptions;
    }

    private void setModelMenuOptions(List<Option> sig) {
        this.modelMenuOptions = sig;
    }

    // ui: dropDown solution
    // @binding
    private DropDown dropDown1 = new DropDown();

    public DropDown getDropDown1() {
        return dropDown1;
    }

    public void setDropDown1(DropDown dd) {
        this.dropDown1 = dd;
    }

    private String currentModelName;

    public String getCurrentModelName() {
        return currentModelName;
    }

    public void setCurrentModelName(String cmn) {
        this.currentModelName = cmn;
    }

    private String modelHelpLinkURL;

    public String getModelHelpLinkURL() {
        return modelHelpLinkURL;
    }

    public void setModelHelpLinkURL(String url) {
        this.modelHelpLinkURL = url;
    }
    /**
     * h:panelGrid component that contains the model help
     * information box in the advanced statistics pane.
     * The rendered attribute of this component must be state-kept.
     * Initially hidden.
     */
    private HtmlPanelGrid gridPanelModelInfoBox = new HtmlPanelGrid();
    
    /**
     * Getter for component gridPanelModelInfoBox
     *
     * @return    h:panelGrid of the model help info box
     */
    public HtmlPanelGrid getGridPanelModelInfoBox() {
        return gridPanelModelInfoBox;
    }
    
    /**
     * Setter for component gridPanelModelInfoBox
     *
     * @param hpg    h:panelGrid of the model help info box
     */
    public void setGridPanelModelInfoBox(HtmlPanelGrid hpg) {
        this.gridPanelModelInfoBox = hpg;
    }
    
    /**
     * The Boolean object backing the rendered attribute of 
     * gridPanelModelInfoBox (h:panelGrid) component.
     * Exposed to the SubsettingPage.jsp.
     * Must be state-kept.
     */
    private Boolean gridPanelModelInfoBoxRendered;

    /**
     * Getter for attribute gridPanelModelInfoBoxRendered
     *
     * @return    the rendered attribute of h:panelGrid
     */    
    public Boolean getGridPanelModelInfoBoxRendered(){
        return groupPanel8belowRendered;
    }
    
    /**
     * Setter for attribute gridPanelModelInfoBoxRendered
     *
     * @param rndrd    the rendered attribute of h:panelGrid
     */
    public void setGridPanelModelInfoBoxRendered(Boolean rndrd){
        gridPanelModelInfoBoxRendered = rndrd;
    }

    // dropDown1: ui: dropDown@valueChangeListener
    public void dropDown1_processValueChange(ValueChangeEvent vce) {
        dbgLog.fine("\n\n***** dropDown1_processValueChange:start *****");        
        String lastModelName = getCurrentModelName();
        dbgLog.fine("stored model name(get)=" + lastModelName);
        dbgLog.fine("stored model name=" + currentModelName);
        FacesContext cntxt = FacesContext.getCurrentInstance();

        // clear message
        resetMsg4MoveVar();
        resetMsgAdvStatButton();
        
        dbgLog.fine("pass the valueChangeListener dropDown1_processValueChange");
        String newModelName = (String) vce.getNewValue();
        dbgLog.fine("Newly selected model=" + newModelName);
        // IE users may select separator "---"
        if (newModelName.startsWith("---")){
            if (lastModelName==null){
                // no model has been chosen but the separator was chosen (1st)
                return;
            } else if (lastModelName.startsWith("---")) {
                // 2nd or later time
                return;
            } else {
                // last option was some meaningful model name
                newModelName = lastModelName;
                dbgLog.fine("revert to the previous choice");
                dbgLog.fine("selected model(---)=" + getCurrentModelName());
                dbgLog.fine("selected model(---)=" + lastModelName);
                setCurrentModelName(lastModelName);
                dropDown1.setSelected(lastModelName);
                dbgLog.fine("selected model(---)=" + dropDown1.getSelected());
                dbgLog.fine("selected model(---)=" + currentModelName);

                cntxt.getExternalContext().getSessionMap().put("currentModelName",
                currentModelName);
            }
        } else {
            // model option was chosen
            // this model's name
            // String selectedModelName= (String)dropDown1.getSelected();
            setCurrentModelName((String) dropDown1.getSelected());
            dbgLog.fine("selected model=" + getCurrentModelName());
            cntxt.getExternalContext().getSessionMap().put("currentModelName",
                currentModelName);
        }
        // this model's spec
        AdvancedStatGUIdata.Model selectedModelSpec = getAnalysisApplicationBean()
            .getSpecMap().get(getCurrentModelName());
        dbgLog.fine("model info:\n" + selectedModelSpec);
        // cntxt.getExternalContext().getSessionMap().put("selectedModelSpec",selectedModelSpec);

        // for the first time only
        //if (!groupPanel8below.isRendered()) {
        if (!groupPanel8belowRendered) {
            dbgLog.fine("this is the first time to render the model-option panel");
            // groupPanel8below.setRendered(true);
            groupPanel8belowRendered=Boolean.TRUE;
        } else {
            dbgLog.fine("this is NOT the first time to render the model-option panel");
            if (!newModelName.equals(lastModelName)) {
                dbgLog.fine("A New Model is selected: Clear all variables in the R boxes");
                advStatVarRBox1.clear();
                advStatVarRBox2.clear();
                advStatVarRBox3.clear();
                resetVarSetAdvStat(varCart);
            }

        }
        String modelHelp = selectedModelSpec.getHelplink();
        if (!StringUtils.isEmpty(modelHelp)) {
            setModelHelpLinkURL(modelHelp);
            FacesContext.getCurrentInstance().getExternalContext()
                 .getSessionMap().put("modelHelpLinkURL", modelHelp);
            gridPanelModelInfoBox.setRendered(true);
        } else {
            gridPanelModelInfoBox.setRendered(false);
        }
        dbgLog.fine("help Link=" + modelHelp);

        // this model's required variable boxes
        int noRboxes = selectedModelSpec.getNoRboxes();
        dbgLog.fine("model info:RBoxes=" + noRboxes);

        if (noRboxes == 1) {
            // hide 2nd/3rd panels
            groupPanel13.setRendered(false);
            groupPanel14.setRendered(false);

            dbgLog.fine("varBoxR1 label="
                + selectedModelSpec.getVarBox().get(0).getLabel());

            // set var box label
            varListbox1Lbl.setText(selectedModelSpec.getVarBox().get(0)
                .getLabel());

        } else if (noRboxes == 2) {
            // open 2nd panel and hide 3rd one
            groupPanel13.setRendered(true);
            groupPanel14.setRendered(false);

            dbgLog.fine("varBoxR1 label="
                + selectedModelSpec.getVarBox().get(0).getLabel());
            dbgLog.fine("varBoxR2 label="
                + selectedModelSpec.getVarBox().get(1).getLabel());

            // set var box label
            varListbox1Lbl.setText(selectedModelSpec.getVarBox().get(0)
                .getLabel());
            varListbox2Lbl.setText(selectedModelSpec.getVarBox().get(1)
                .getLabel());

        } else if (noRboxes == 3) {
            // open 2nd/3rd panels
            groupPanel13.setRendered(true);
            groupPanel14.setRendered(true);

            dbgLog.fine("varBoxR1 label="
                + selectedModelSpec.getVarBox().get(0).getLabel());
            dbgLog.fine("varBoxR2 label="
                + selectedModelSpec.getVarBox().get(1).getLabel());
            dbgLog.fine("varBoxR3 label="
                + selectedModelSpec.getVarBox().get(2).getLabel());

            // set var box label
            varListbox1Lbl.setText(selectedModelSpec.getVarBox().get(0)
                .getLabel());
            varListbox2Lbl.setText(selectedModelSpec.getVarBox().get(1)
                .getLabel());
            varListbox3Lbl.setText(selectedModelSpec.getVarBox().get(2)
                .getLabel());

        }

        // set up option panels
        if (getCurrentModelName().equals("xtb")) {
            // cross-tabulation (non-zelig)
            checkboxGroup2.setRendered(false);
            chkbxAdvStatOutputOpt.setRendered(false);
            checkboxGroupXtb.setRendered(true);
            chkbxAdvStatOutputXtbOpt.setRendered(true);
            analysisOptionPanel.setRendered(false);
            
        } else {
            // zelig
            if (selectedModelSpec.getMaxSetx() == 0) {
                // hide analysis option panel such as factor models
                setxOptionPanel.setRendered(false);
                analysisOptionPanel.setRendered(false);
            } else {
            checkboxGroup2.setRendered(true);
            chkbxAdvStatOutputOpt.setRendered(true);
            chkbxAdvStatOutputXtbOpt.setRendered(false);
            checkboxGroupXtb.setRendered(false);
            analysisOptionPanel.setRendered(true);
            // show/hide setx-option panel
                setxOptionPanel.setRendered(true);
            }
        }
        FacesContext.getCurrentInstance().getExternalContext()
            .getSessionMap().put("groupPanel8belowRendered", 
            groupPanel8belowRendered);
        dbgLog.fine("groupPanel8belowRendered=" + groupPanel8belowRendered);
        cntxt.renderResponse();
        dbgLog.fine("***** dropDown1_processValueChange:end *****\n");
    }

    private String dropDown1ClientId;

    public String getDropDown1ClientId() {
        FacesContext cntxt = FacesContext.getCurrentInstance();
        return dropDown1.getClientId(cntxt);
    }

    // panel below the dropDown Menu
    
    /**
     * The PanelGroup object backing the binding attribute of 
     * ui:panelGroup component that covers the pane below the dropDown 
     * model-selection menu in the advanced statistics pane.
     * The rendered attribute of this component must be state-kept.
     * Initially hidden.
     */
    private PanelGroup groupPanel8below = new PanelGroup();
    /**
     * Getter for component groupPanel8below
     *
     * @return    ui:panelGroup of the pane below the model-selection menu
     */
    public PanelGroup getGroupPanel8below() {
        return groupPanel8below;
    }
    /**
     * Setter for component groupPanel8below
     *
     * @param pg    ui:panelGroup of the pane below the model-selection menu
     */
    public void setGroupPanel8below(PanelGroup pg) {
        this.groupPanel8below = pg;
    }

    /**
     * The Boolean object backing the rendered attribute of 
     * groupPanel8below (ui:panelGroup) component.
     * Exposed to the SubsettingPage.jsp.
     * Must be state-kept. 
     * Added after serialization problems of the component-binding
     */
    private Boolean groupPanel8belowRendered;

    /**
     * Getter for attribute groupPanel8belowRendered
     *
     * @return    the rendered attribute of ui:panelGroup
     */    
    public Boolean getGroupPanel8belowRendered(){
        return groupPanel8belowRendered;
    }

    /**
     * Setter for attribute groupPanel8belowRendered
     *
     * @param rndrd    the rendered attribute of ui:panelGroup
     */
    public void setGroupPanel8belowRendered(Boolean rndrd){
        groupPanel8belowRendered = rndrd;
    }

    // label@binding for variable Left box1
    private Label varListbox1Lbl = new Label();

    public Label getVarListbox1Lbl() {
        return varListbox1Lbl;
    }

    public void setVarListbox1Lbl(Label l) {
        this.varListbox1Lbl = l;
    }

    // label@binding for variable box2
    private Label varListbox2Lbl = new Label();

    public Label getVarListbox2Lbl() {
        return varListbox2Lbl;
    }

    public void setVarListbox2Lbl(Label l) {
        this.varListbox2Lbl = l;
    }

    // label@binding for variable box3
    private Label varListbox3Lbl = new Label();

    public Label getVarListbox3Lbl() {
        return varListbox3Lbl;
    }

    public void setVarListbox3Lbl(Label l) {
        this.varListbox3Lbl = l;
    }

    // panel for the 1st var box
    // PanelGroup@binding
    private PanelGroup groupPanel12 = new PanelGroup();

    public PanelGroup getGroupPanel12() {
        return groupPanel12;
    }

    public void setGroupPanel12(PanelGroup pg) {
        this.groupPanel12 = pg;
    }

    // panel for the 2nd var box
    // PanelGroup@binding
    private PanelGroup groupPanel13 = new PanelGroup();

    public PanelGroup getGroupPanel13() {
        return groupPanel13;
    }

    public void setGroupPanel13(PanelGroup pg) {
        this.groupPanel13 = pg;
    }

    // plane for the 3rd var box
    // PanelGroup@binding
    private PanelGroup groupPanel14 = new PanelGroup();

    public PanelGroup getGroupPanel14() {
        return groupPanel14;
    }

    public void setGroupPanel14(PanelGroup pg) {
        this.groupPanel14 = pg;
    }

    // boxL1:listbox@binding
    private Listbox listboxAdvStat = new Listbox();

    public Listbox getListboxAdvStat() {
        return listboxAdvStat;
    }

    public void setListboxAdvStat(Listbox lstbx) {
        this.listboxAdvStat = lstbx;
    }

    // boxL1: listbox@items (data for the listbox)

    private Collection<Option> varSetAdvStat = new ArrayList<Option>();

    public Collection<Option> getVarSetAdvStat() {
        return varSetAdvStat;
    }

    public void setVarSetAdvStat(Collection<Option> co) {
        this.varSetAdvStat = co;
    }

    // re-populating the list of Options
    public void resetVarSetAdvStat(Map vs) {
        if (!varSetAdvStat.isEmpty()) {
            varSetAdvStat.clear();
        }

        /*
         * dbgLog.fine("resetVarSetAdvStat: current tab
         * id="+tabSet1.getSelected()); if
         * (tabSet1.getSelected().equals("tabRecode")){ dbgLog.fine("current tab
         * is: Recode[no recoded variables added]"); } else{
         * dbgLog.fine("current tab is not Recode [add recoded variables]"); }
         */

        Iterator all = vs.entrySet().iterator();
        while (all.hasNext()) {
            Entry entry = (Entry) all.next();
            varSetAdvStat.add(new Option(entry.getKey().toString(),
                (String) entry.getValue()));
        }
    }

    // boxL1: listbox@selected : storage object for selected value
    private String[] advStatSelectedVarLBox;

    public String[] getAdvStatSelectedVarLBox() {
        return advStatSelectedVarLBox;
    }

    public void setAdvStatSelectedVarLBox(String[] dol) {
        this.advStatSelectedVarLBox = dol;
    }

    // boxR1: listbox@binding
    // 
    private Listbox advStatVarListboxR1 = new Listbox();

    public Listbox getAdvStatVarListboxR1() {
        return advStatVarListboxR1;
    }

    public void setAdvStatVarListboxR1(Listbox l1) {
        this.advStatVarListboxR1 = l1;
    }

    // boxR1 : listbox@items [provide data for the listbox]

    private Collection<Option> advStatVarRBox1 = new ArrayList<Option>();

    public Collection<Option> getAdvStatVarRBox1() {
        return advStatVarRBox1;
    }

    public void setAdvStatVarRBox1(Collection<Option> dol) {
        this.advStatVarRBox1 = dol;
    }

    // boxR1: listbox@selected
    private String[] advStatSelectedVarRBox1;

    public String[] getAdvStatSelectedVarRBox1() {
        return advStatSelectedVarRBox1;
    }

    public void setAdvStatSelectedVarRBox1(String[] dol) {
        this.advStatSelectedVarRBox1 = dol;
    }

    // boxR2: listbox@binding
    private Listbox advStatVarListboxR2 = new Listbox();

    public Listbox getAdvStatVarListboxR2() {
        return advStatVarListboxR2;
    }

    public void setAdvStatVarListboxR2(Listbox l2) {
        this.advStatVarListboxR2 = l2;
    }

    // boxR2: listbox@items [provide data for the listbox]
    private Collection<Option> advStatVarRBox2 = new ArrayList<Option>();

    public Collection<Option> getAdvStatVarRBox2() {
        return advStatVarRBox2;
    }

    public void setAdvStatVarRBox2(Collection<Option> dol) {
        this.advStatVarRBox2 = dol;
    }

    // boxR2: listbox@selected
    private String[] advStatSelectedVarRBox2;

    public String[] getAdvStatSelectedVarRBox2() {
        return advStatSelectedVarRBox2;
    }

    public void setAdvStatSelectedVarRBox2(String[] dol) {
        this.advStatSelectedVarRBox2 = dol;
    }

    // boxR3: listbox@binding
    private Listbox advStatVarListboxR3 = new Listbox();

    public Listbox getAdvStatVarListboxR3() {
        return advStatVarListboxR3;
    }

    public void setAdvStatVarListboxR3(Listbox l3) {
        this.advStatVarListboxR3 = l3;
    }

    // boxR3: listbox@items [provide data for the listbox]
    private Collection<Option> advStatVarRBox3 = new ArrayList<Option>();

    public Collection<Option> getAdvStatVarRBox3() {
        return advStatVarRBox3;
    }

    public void setAdvStatVarRBox3(Collection<Option> dol) {
        this.advStatVarRBox3 = dol;
    }

    // boxR3: listbox@selected
    private String[] advStatSelectedVarRBox3;

    public String[] getAdvStatSelectedVarRBox3() {
        return advStatSelectedVarRBox3;
    }

    public void setAdvStatSelectedVarRBox3(String[] dol) {
        this.advStatSelectedVarRBox3 = dol;
    }

    // moveVar1Bttn
    // > button (add)
    // @binding
    private HtmlCommandButton button4 = new HtmlCommandButton();

    public HtmlCommandButton getButton4() {
        return button4;
    }

    public void setButton4(HtmlCommandButton hcb) {
        this.button4 = hcb;
    }

    // < button (remove)
    // @binding
    private HtmlCommandButton button4b = new HtmlCommandButton();

    public HtmlCommandButton getButton4b() {
        return button4b;
    }

    public void setButton4b(HtmlCommandButton hcb) {
        this.button4 = hcb;
    }

    // moveVar2Bttn
    // > button (add)
    // @binding
    private HtmlCommandButton button5 = new HtmlCommandButton();

    public HtmlCommandButton getButton5() {
        return button5;
    }

    public void setButton5(HtmlCommandButton hcb) {
        this.button5 = hcb;
    }

    // < button (remove)
    // @binding
    private HtmlCommandButton button5b = new HtmlCommandButton();

    public HtmlCommandButton getButton5b() {
        return button5b;
    }

    public void setButton5b(HtmlCommandButton hcb) {
        this.button5b = hcb;
    }

    // moveVar3Bttn
    // > button (add)
    // @binding
    private HtmlCommandButton button6 = new HtmlCommandButton();

    public HtmlCommandButton getButton6() {
        return button6;
    }

    public void setButton6(HtmlCommandButton hcb) {
        this.button6 = hcb;
    }

    // < button (remove)
    // @binding
    private HtmlCommandButton button6b = new HtmlCommandButton();

    public HtmlCommandButton getButton6b() {
        return button6b;
    }

    public void setButton6b(HtmlCommandButton hcb) {
        this.button6b = hcb;
    }
    // </editor-fold>
    // <editor-fold desc="Adv Stat  movement">    
    
    
    // get variable type (int) from a given row of the dataTable
    public int getVariableType(DataVariable dv) {
        Integer varType;

        if (dv.getVariableFormatType().getName().equals("numeric")) {
            if (dv.getVariableIntervalType().getName().equals("continuous")) {
                varType = Integer.valueOf("2");
            } else {
                varType = Integer.valueOf("1");
            }
        } else {
            varType = Integer.valueOf("0");
        }
        dbgLog.fine("within: getVariableType: varType= "+varType);
        return varType.intValue();
    }

    // character var or not
    public boolean isCharacterVariable(String newVarId) {
        boolean chr = false;
        Pattern p = Pattern.compile("[a-zA-Z_]+");
        int counter = 0;
        // get the recode table by varId
        List<Object> rvtbl = (List<Object>) recodeSchema.get(newVarId);
        for (int i = 0; i < rvtbl.size(); i++) {
            List<Object> rvs = (List<Object>) rvtbl.get(i);
            // check character or not
            String value = (String) rvs.get(1);
            Matcher m = p.matcher(value);
            if (m.find()) {
                counter++;
            }
        }
        if (counter > 0) {
            chr = true;
        }
        return chr;
    }
    
    /**
     * Returns the number of valid and unique categories (String type) 
     * in a recode table (List) that is stored in recodeSchema (Map) 
     * to check whether this variable is binary, etc.
     * 
     * @param newVarId    the id of a requested variable
     * @return    the number of valid and unique categories
     */
    public int getValidCategories(String newVarId) {
        int noCat = 0;
        // use a Set object to find a unique set of values (String)
        Set<String> newValidValueSet = new HashSet<String>(); 
        // get the recode table by varId
        List<Object> rvtbl = (List<Object>) recodeSchema.get(newVarId);
        for (int i = 0; i < rvtbl.size(); i++) {
            List<Object> rvs = (List<Object>) rvtbl.get(i);
            if (!((Boolean) rvs.get(0))) {
                // 1st element tells whether this value is to be excuded
                // false means "not to exclude this value"
                
                // add this value to the set
                newValidValueSet.add((String)rvs.get(1));
                noCat++;
            }
        }
        dbgLog.fine("non-exclude values="+noCat);
        dbgLog.fine("unique non-exclude values="+newValidValueSet.size());
        //return noCat;
        return newValidValueSet.size();
    }
    
    /**
     * Returns the number of summary statistics of the requested variable
     * 
     * @param varId    the id of a requested variable
     * @return    the number of summary statistics (8 for continuous variables
     *            and at least 3 for non-continuous ones)
     */
    public int getSumStatSize(String varId) {
        int sumStatSize = getVariableById(varId).getSummaryStatistics().size();
        dbgLog.fine("sumStat size=" + sumStatSize);
        return sumStatSize;
    }
    
    /**
     * Returns the number of valid (non-missing-value) values of
     * the requested variable
     * 
     * @param varId    the id of a requested variable
     * @return    the number of valid (non-missing-value) responses
     */
    public int getCatStatSize(String varId) {
        Collection<VariableCategory> catStatSet = getVariableById(varId)
            .getCategories();
        // count non-missing category only
        int catStatSize = 0;
        for (Iterator elc = catStatSet.iterator(); elc.hasNext();) {
            VariableCategory dvcat = (VariableCategory) elc.next();
            if (!dvcat.isMissing()) {
                catStatSize++;
            }
        }
        dbgLog.fine("valid categories=" + catStatSize);
        return catStatSize;
    }

    // check whether the requested move is permissible
    private Boolean checkVarType(String varId, String boxVarType, Boolean strict) {
        dbgLog.fine("variable id=" + varId);
        int varType = 1;
        if (isRecodedVar(varId)) {
            // recoded var case
            if (isCharacterVariable(varId)) {
                // 
                varType = 0;
            }
        } else {
            // existing var case
            // DataVariable dv = getVariableById(varId);
            varType = getVariableType(getVariableById(varId));
        }

        dbgLog.fine("Type of the variable to be moved=" + varType);
        Integer boxType = dataType2Int.get(boxVarType);

        Boolean result = false;
        switch (boxType != null ? boxType : -1) {
            case 5:
                // continuous
                if (strict) {
                    // stricter test
                    if (varType == 2) {
                        result = true;
                    } else {
                        result = false;
                    }
                } else {
                    if (varType != 0) {
                        result = true;
                    } else {
                        result = false;
                    }
                }

                break;
            case 4:
                // discrete
                // this categorization is zelig-context
                if (strict) {
                    // stricter test
                    if ((varType != 0) && (varType != 2)) {
                        result = true;
                    } else {
                        result = false;
                    }
                } else {
                    if (varType != 0) {
                        result = true;
                    } else {
                        result = false;
                    }
                }

                break;
            case 3:
                // ordinal
                if (strict) {
                    // stricter test
                    if ((varType != 0) && (varType != 2)) {
                        result = true;
                    } else {
                        result = false;
                    }
                } else {
                    if (varType != 0) {
                        result = true;
                    } else {
                        result = false;
                    }
                }
                break;

            case 2:
                // nominal
                // no stricter case
                if (varType == 0) {
                    result = true;
                } else {
                    result = false;
                }
                break;

            case 23:
            case 32:
                // nominal|ordinal
                // no stricter case
                if (varType != 2) {
                    result = true;
                } else {
                    result = false;
                }
                break;

            case 1:
                // binary
                int sumStatSize = 0;
                int catStatSize = 0;
                if (isRecodedVar(varId)) {
                    catStatSize = getValidCategories(varId);
                } else {
                    sumStatSize = getSumStatSize(varId);
                    catStatSize = getCatStatSize(varId);
                }
                if ((varType == 2) || (sumStatSize > 2) || (catStatSize < 2)) {
                    // continuous var or more-than-ten-categories (=> not
                    // binary)
                    result = false;
                } else {
                    // net test
                    if (catStatSize == 2) {
                        result = true;
                    } else {
                        result = false;
                    }
                }

                break;
            case 0:
                // any
                // do nothing
                result = true;

                break;
            default:
                result = true;
                break;
        }

        if (result) {
            dbgLog.fine("The move passed the test");
        } else {
            dbgLog.fine("The move failed the test");
            dbgLog.fine("expected type=" + boxVarType);
            dbgLog.fine("variable's type="
                + vtInt2String.get(Integer.toString(varType)));
        }
        return result;
    }

    // remove the selected option from a given listbox@items
    private Option removeOption(String varId, Collection<Option> co) {
        Option matchedOptn = null;
        Iterator iter = co.iterator();
        while (iter.hasNext()) {
            Option optn = (Option) iter.next();
            if (varId.equals(optn.getValue())) {
                matchedOptn = optn;
                co.remove(optn);
                break;
            }
        }
        return matchedOptn;
    }

    // reset all error message text-fields related to moveVar buttons
    public void resetMsg4MoveVar() {
        msgMoveVar1Bttn.setRendered(false);
        msgMoveVar1Bttn.setText(" ");
        msgMoveVar2Bttn.setRendered(false);
        msgMoveVar2Bttn.setText(" ");
        msgMoveVar3Bttn.setRendered(false);
        msgMoveVar3Bttn.setText(" ");
    }

    private StaticText msgMoveVar1Bttn = new StaticText();

    public StaticText getMsgMoveVar1Bttn() {
        return msgMoveVar1Bttn;
    }

    public void setMsgMoveVar1Bttn(StaticText txt) {
        this.msgMoveVar1Bttn = txt;
    }

    // move from L to R1
    public void addVarBoxR1(ActionEvent acev) {
        dbgLog.fine("\n***** within addVarBoxR1(): model name=" + 
            getCurrentModelName()+" *****");
        
        resetMsg4MoveVar();
        resetMsgAdvStatButton();
        
        dbgLog.fine("advStatSelectedVarLBox="+advStatSelectedVarLBox);
        dbgLog.fine("advStatSelectedVar from binding="+listboxAdvStat.getSelected());
        String[] OptnSet = null;
        if (getAdvStatSelectedVarLBox() == null){
            OptnSet = (String[])listboxAdvStat.getSelected();
        } else {
            OptnSet = getAdvStatSelectedVarLBox();
        }
        
        dbgLog.fine("OptnSet Length="+OptnSet.length);
        dbgLog.fine("OptnSet="+OptnSet);

        int BoxR1max = getAnalysisApplicationBean().getSpecMap().get(
            getCurrentModelName()).getVarBox().get(0).getMaxvar();

        dbgLog.fine("BoxR1max=" + BoxR1max);

        // dbgLog.fine("BoxR1min="+getAnalysisApplicationBean().getSpecMap().get(getCurrentModelName()).getVarBox().get(0).getMinvar()
        // );

        dbgLog.fine("current listboxR1 size=" + advStatVarRBox1.size());

        String varType = getAnalysisApplicationBean().getSpecMap().get(
            getCurrentModelName()).getVarBox().get(0).getVarType();

        dbgLog.fine("permissible variable type for advStatVarRBox1=" + varType);

        // for each selected item
        if (advStatVarRBox1.size() < BoxR1max) {
            dbgLog.fine("< BoxR1max case");
            for (int i = 0; i < OptnSet.length; i++) {
                // reset error message field
                resetMsg4MoveVar();
                // type check
                dbgLog.fine("OptnSet[" + i + "]=" + OptnSet[i]);
                if (checkVarType(OptnSet[i], varType, true)) {
                    getAdvStatVarRBox1().add(
                        removeOption(OptnSet[i], getVarSetAdvStat()));

                    dbgLog.fine("current listboxR1 size(within loop)="
                        + advStatVarRBox1.size());
                    if (advStatVarRBox1.size() == BoxR1max) {
                        return;
                    }
                } else {
                    // show error message
                    msgMoveVar1Bttn.setText("* Incompatible type:<br />required="
                            + varType
                            + "<br />found="
                            + vtInt2String
                                .get(Integer
                                    .toString(getVariableType(getVariableById(OptnSet[i])))));
                    msgMoveVar1Bttn.setRendered(true);
                }
            }
        } else {
            dbgLog.fine("1st RHS box is already maxed out");
            // show error message;
            msgMoveVar1Bttn.setText("* The max number of variables<br/>for this box is: "
                    + BoxR1max);
            msgMoveVar1Bttn.setRendered(true);
        }
    }

    private StaticText msgMoveVar2Bttn = new StaticText();

    public StaticText getMsgMoveVar2Bttn() {
        return msgMoveVar2Bttn;
    }

    public void setMsgMoveVar2Bttn(StaticText txt) {
        this.msgMoveVar2Bttn = txt;
    }

    // move from L to R2
    public void addVarBoxR2(ActionEvent acev) {
        // set left-selected items to a temp list
        //String[] OptnSet = (String[])getAdvStatSelectedVarLBox();

        dbgLog.fine("***** within addVarBoxR2(): model name=" +
            getCurrentModelName()+" *****");
        
        resetMsg4MoveVar();
        resetMsgAdvStatButton();
        
        dbgLog.fine("advStatSelectedVarLBox="+advStatSelectedVarLBox);
        dbgLog.fine("advStatSelectedVar from binding="+listboxAdvStat.getSelected());
        String[] OptnSet = null;
        if (getAdvStatSelectedVarLBox() == null){
            OptnSet = (String[])listboxAdvStat.getSelected();
        } else {
            OptnSet = getAdvStatSelectedVarLBox();
        }
        
        dbgLog.fine("OptnSet Length="+OptnSet.length);
        dbgLog.fine("OptnSet="+OptnSet);
        
        int BoxR2max = getAnalysisApplicationBean().getSpecMap().get(
            getCurrentModelName()).getVarBox().get(1).getMaxvar();

        dbgLog.fine("BoxR2max=" + BoxR2max);

        // dbgLog.fine("BoxR2min="+getAnalysisApplicationBean().getSpecMap().get(getCurrentModelName()).getVarBox().get(1).getMinvar()
        // );

        dbgLog.fine("current listbox size=" + advStatVarRBox2.size());

        String varType = getAnalysisApplicationBean().getSpecMap().get(
            getCurrentModelName()).getVarBox().get(1).getVarType();

        dbgLog.fine("permissible variable type for advStatVarRBox2=" + varType);

        // for each selected item
        if (advStatVarRBox2.size() < BoxR2max) {

            for (int i = 0; i < OptnSet.length; i++) {
                // reset error message field
                resetMsg4MoveVar();
                // type check
                dbgLog.fine("OptnSet[" + i + "]=" + OptnSet[i]);
                if (checkVarType(OptnSet[i], varType, true)) {
                    getAdvStatVarRBox2().add(
                        removeOption(OptnSet[i], getVarSetAdvStat()));
                    dbgLog.fine("current listboxR2 size(within loop)="
                        + advStatVarRBox2.size());
                    if (advStatVarRBox2.size() == BoxR2max) {
                        return;
                    }
                } else {
                    // show error message
                    msgMoveVar2Bttn.setText("* Incompatible type:<br />required="
                            + varType
                            + "<br />found="
                            + vtInt2String
                                .get(Integer
                                    .toString(getVariableType(getVariableById(OptnSet[i])))));
                    msgMoveVar2Bttn.setRendered(true);
                }
            }
        } else {
            dbgLog.warning("2nd RHS box is already maxed out");
            // show error message;
            msgMoveVar2Bttn.setText("* The max number of variables<br/>for this box is: "
                    + BoxR2max);
            msgMoveVar2Bttn.setRendered(true);
        }
    }

    private StaticText msgMoveVar3Bttn = new StaticText();

    public StaticText getMsgMoveVar3Bttn() {
        return msgMoveVar3Bttn;
    }

    public void setMsgMoveVar3Bttn(StaticText txt) {
        this.msgMoveVar3Bttn = txt;
    }

    // move from L to R3
    public void addVarBoxR3(ActionEvent acev) {
        // set left-selected items to a temp list
        //String[] OptnSet = (String[])getAdvStatSelectedVarLBox();

        dbgLog.fine("***** within addVarBoxR3(): model name=" + 
            getCurrentModelName()+" *****");
        
        resetMsg4MoveVar();
        resetMsgAdvStatButton();
        
        dbgLog.fine("advStatSelectedVarLBox="+advStatSelectedVarLBox);
        dbgLog.fine("advStatSelectedVar from binding="+listboxAdvStat.getSelected());
        String[] OptnSet = null;
        if (getAdvStatSelectedVarLBox() == null){
            OptnSet = (String[])listboxAdvStat.getSelected();
        } else {
            OptnSet = getAdvStatSelectedVarLBox();
        }
        
        dbgLog.fine("OptnSet Length="+OptnSet.length);
        dbgLog.fine("OptnSet="+OptnSet);
        
        int BoxR3max = getAnalysisApplicationBean().getSpecMap().get(
            getCurrentModelName()).getVarBox().get(2).getMaxvar();

        dbgLog.fine("BoxR3max=" + BoxR3max);

        // dbgLog.fine("BoxR2min="+getAnalysisApplicationBean().getSpecMap().get(getCurrentModelName()).getVarBox().get(2).getMinvar()
        // );

        dbgLog.fine("current listboxR3 size=" + advStatVarRBox3.size());

        String varType = getAnalysisApplicationBean().getSpecMap().get(
            getCurrentModelName()).getVarBox().get(2).getVarType();

        dbgLog.fine("permissible variable type for advStatVarRBox3=" + varType);

        // for each selected item
        if (advStatVarRBox3.size() < BoxR3max) {
            for (int i = 0; i < OptnSet.length; i++) {
                // reset error message field
                resetMsg4MoveVar();
                // type check
                dbgLog.fine("OptnSet[" + i + "]=" + OptnSet[i]);
                if (checkVarType(OptnSet[i], varType, true)) {

                    getAdvStatVarRBox3().add(
                        removeOption(OptnSet[i], getVarSetAdvStat()));

                    dbgLog.fine("current listboxR3 size(within loop)="
                        + advStatVarRBox3.size());
                    if (advStatVarRBox3.size() == BoxR3max) {
                        return;
                    }
                } else {
                    // show error message
                    msgMoveVar3Bttn.setText("* Incompatible type:<br />required="
                            + varType
                            + "<br />found="
                            + vtInt2String
                                .get(Integer
                                    .toString(getVariableType(getVariableById(OptnSet[i])))));
                    msgMoveVar3Bttn.setRendered(true);
                }
            }
        }
    }

    // move from R1 to L
    public void removeVarBoxR1(ActionEvent acev) {
        
        dbgLog.fine("************  within removeVarBoxR1() ************");
        // set left-selected items to a temp list
        //String[] OptnSet = getAdvStatSelectedVarRBox1();
        
        
        resetMsg4MoveVar();
        resetMsgAdvStatButton();
        
        
        dbgLog.fine("advStatSelectedVarRBox1="+advStatSelectedVarRBox1);
        dbgLog.fine("advStatSelectedVar from binding="+advStatVarListboxR1.getSelected());
        String[] OptnSet = null;
        if (getAdvStatSelectedVarRBox1() == null){
            OptnSet = (String[])advStatVarListboxR1.getSelected();
        } else {
            OptnSet = getAdvStatSelectedVarRBox1();
        }
        
        dbgLog.fine("OptnSet Length="+OptnSet.length);
        dbgLog.fine("OptnSet="+OptnSet);
        // for each selected item
        for (int i = 0; i < OptnSet.length; i++) {
            getVarSetAdvStat().add(
                removeOption(OptnSet[i], getAdvStatVarRBox1()));
        }
    }

    // move from R2 to L
    public void removeVarBoxR2(ActionEvent acev) {
        dbgLog.fine("************  within removeVarBoxR2() ************");
        // set left-selected items to a temp array, Option []
        //String[] OptnSet = getAdvStatSelectedVarRBox2();
        
        resetMsg4MoveVar();
        resetMsgAdvStatButton();
        
        dbgLog.fine("advStatSelectedVarRBox2="+advStatSelectedVarRBox2);
        dbgLog.fine("advStatSelectedVar from binding="+advStatVarListboxR2.getSelected());
        String[] OptnSet = null;
        if (getAdvStatSelectedVarRBox2() == null){
            OptnSet = (String[])advStatVarListboxR2.getSelected();
        } else {
            OptnSet = getAdvStatSelectedVarRBox2();
        }
        
        dbgLog.fine("OptnSet Length="+OptnSet.length);
        dbgLog.fine("OptnSet="+OptnSet);
        
        // for each selected item
        for (int i = 0; i < OptnSet.length; i++) {
            getVarSetAdvStat().add(
                removeOption(OptnSet[i], getAdvStatVarRBox2()));
        }
    }

    // move from R3 to L
    public void removeVarBoxR3(ActionEvent acev) {
        dbgLog.fine("************  within removeVarBoxR3() ************");
        // set left-selected items to a temp array, Option []
        //String[] OptnSet = getAdvStatSelectedVarRBox3();
        
        resetMsg4MoveVar();
        resetMsgAdvStatButton();
        
        dbgLog.fine("advStatSelectedVarRBox3="+advStatSelectedVarRBox3);
        dbgLog.fine("advStatSelectedVar from binding="+advStatVarListboxR3.getSelected());
        String[] OptnSet = null;
        if (getAdvStatSelectedVarRBox3() == null){
            OptnSet = (String[])advStatVarListboxR3.getSelected();
        } else {
            OptnSet = getAdvStatSelectedVarRBox3();
        }
        
        dbgLog.fine("OptnSet Length="+OptnSet.length);
        dbgLog.fine("OptnSet="+OptnSet);        
        
        
        
        // for each selected item
        for (int i = 0; i < OptnSet.length; i++) {
            getVarSetAdvStat().add(
                removeOption(OptnSet[i], getAdvStatVarRBox3()));
        }
    }
    // </editor-fold>
    // ////////////////////
    // Output option
    // ////////////////////
    // <editor-fold desc="Adv Stat Output option">
    // output option panel

    /*
     * private PanelGroup groupPanel25 = new PanelGroup();
     * 
     * public PanelGroup getGroupPanel25() { return groupPanel25; }
     * 
     * public void setGroupPanel25(PanelGroup pg) { this.groupPanel25 = pg; }
     */

    // output option checkbox group: zelig
    private CheckboxGroup checkboxGroup2 = new CheckboxGroup();

    public CheckboxGroup getCheckboxGroup2() {
        return checkboxGroup2;
    }

    public void setCheckboxGroup2(CheckboxGroup cg) {
        this.checkboxGroup2 = cg;
    }

    private MultipleSelectOptionsList checkboxGroup2DefaultOptions = new MultipleSelectOptionsList();

    public MultipleSelectOptionsList getCheckboxGroup2DefaultOptions() {
        return checkboxGroup2DefaultOptions;
    }

    public void setCheckboxGroup2DefaultOptions(MultipleSelectOptionsList msol) {
        this.checkboxGroup2DefaultOptions = msol;
    }






    private HtmlSelectManyCheckbox chkbxAdvStatOutputOpt = new HtmlSelectManyCheckbox();

    public HtmlSelectManyCheckbox getChkbxAdvStatOutputOpt() {
        return chkbxAdvStatOutputOpt;
    }

    public void setChkbxAdvStatOutputOpt(HtmlSelectManyCheckbox chkbxAdvStatOutputOpt) {
        this.chkbxAdvStatOutputOpt = chkbxAdvStatOutputOpt;
    }


    public Map<String, String> chkbxAdvStatOutputOptMap = new LinkedHashMap<String, String>();

    public Map<String, String> getChkbxAdvStatOutputOptMap() {
        return chkbxAdvStatOutputOptMap;
    }

    public void setChkbxAdvStatOutputOptMap(Map<String, String> chkbxAdvStatOutputOptMap) {
        this.chkbxAdvStatOutputOptMap = chkbxAdvStatOutputOptMap;
    }


    private HtmlSelectManyCheckbox chkbxAdvStatOutputXtbOpt = new HtmlSelectManyCheckbox();

    public HtmlSelectManyCheckbox getChkbxAdvStatOutputXtbOpt() {
        return chkbxAdvStatOutputXtbOpt;
    }

    public void setChkbxAdvStatOutputXtbOpt(HtmlSelectManyCheckbox chkbxAdvStatOutputXtbOpt) {
        this.chkbxAdvStatOutputXtbOpt = chkbxAdvStatOutputXtbOpt;
    }


    public Map<String, String> chkbxAdvStatOutputXtbOptMap = new LinkedHashMap<String, String>();

    public Map<String, String> getChkbxAdvStatOutputXtbOptMap() {
        return chkbxAdvStatOutputXtbOptMap;
    }

    public void setChkbxAdvStatOutputXtbOptMap(Map<String, String> chkbxAdvStatOutputXtbOptMap) {
        this.chkbxAdvStatOutputXtbOptMap = chkbxAdvStatOutputXtbOptMap;
    }


    // output option checkbox group: xtab
    private CheckboxGroup checkboxGroupXtb = new CheckboxGroup();

    public CheckboxGroup getCheckboxGroupXtb() {
        return checkboxGroupXtb;
    }

    public void setCheckboxGroupXtb(CheckboxGroup cg) {
        this.checkboxGroupXtb = cg;
    }

    private MultipleSelectOptionsList checkboxGroupXtbOptions = new MultipleSelectOptionsList();

    public MultipleSelectOptionsList getCheckboxGroupXtbOptions() {
        return checkboxGroupXtbOptions;
    }

    public void setCheckboxGroupXtbOptions(MultipleSelectOptionsList msol) {
        this.checkboxGroupXtbOptions = msol;
    }

    public void checkboxGroupXtbProcessValueChange(ValueChangeEvent vce) {
        dbgLog.fine("checkboxGroupXtbProcessValueChange");
        dbgLog.fine("checkbox: new value=" + vce.getNewValue());
        Option[] outOption = (Option[]) checkboxGroupXtbOptions.getOptions();
        for (int i = 0; i < outOption.length; i++) {
            dbgLog.fine("output option[" + i + "]=" + outOption[i].getValue());
        }
    }
    // </editor-fold>
    // ////////////////////
    // Analysis option
    // ////////////////////
    // <editor-fold desc="Adv Stat Analysis Option">
    // Analysis option block: casing panel

    private PanelGroup analysisOptionPanel = new PanelGroup();

    public PanelGroup getAnalysisOptionPanel() {
        return analysisOptionPanel;
    }

    public void setAnalysisOptionPanel(PanelGroup pg) {
        this.analysisOptionPanel = pg;
    }

    // setx-option panel
    private PanelGroup setxOptionPanel = new PanelGroup();

    public PanelGroup getSetxOptionPanel() {
        return setxOptionPanel;
    }

    public void setSetxOptionPanel(PanelGroup pg) {
        this.setxOptionPanel = pg;
    }

    // simulation: casing panel
    private PanelGroup groupPanel20 = new PanelGroup();

    public PanelGroup getGroupPanel20() {
        return groupPanel20;
    }

    public void setGroupPanel20(PanelGroup pg) {
        this.groupPanel20 = pg;
    }

    private HtmlPanelGroup groupPanelSimTypeChoice = new HtmlPanelGroup();

    public HtmlPanelGroup getGroupPanelSimTypeChoice() {
        return groupPanelSimTypeChoice;
    }

    public void setGroupPanelSimTypeChoice(HtmlPanelGroup groupPanelSimTypeChoice) {
        this.groupPanelSimTypeChoice = groupPanelSimTypeChoice;
    }




    // simulation option: checkbox

    private Checkbox checkbox3 = new Checkbox();

    public Checkbox getCheckbox3() {
        return checkbox3;
    }

    public void setCheckbox3(Checkbox c) {
        this.checkbox3 = c;
    }

    public void showHideSimulationsOptPanel(ValueChangeEvent vce) {
    
        Boolean currentState = (Boolean) vce.getNewValue();
        if ((currentState.toString()).equals("true")) {
            //groupPanel20.setRendered(true);
            groupPanelSimTypeChoice.setRendered(true);
        } else if ((currentState.toString()).equals("false")) {
            //groupPanel20.setRendered(false);
            groupPanelSimTypeChoice.setRendered(false);
        }

        FacesContext.getCurrentInstance().renderResponse();
    }


    // simulation option: radio button group
    // @binding
    private RadioButtonGroup radioButtonGroup1 = new RadioButtonGroup();

    public RadioButtonGroup getRadioButtonGroup1() {
        return radioButtonGroup1;
    }

    public void setRadioButtonGroup1(RadioButtonGroup rbg) {
        this.radioButtonGroup1 = rbg;
    }

    // @items
    private SingleSelectOptionsList radioButtonGroup1DefaultOptions = new SingleSelectOptionsList();

    public SingleSelectOptionsList getRadioButtonGroup1DefaultOptions() {
        return radioButtonGroup1DefaultOptions;
    }

    public void setRadioButtonGroup1DefaultOptions(SingleSelectOptionsList ssol) {
        this.radioButtonGroup1DefaultOptions = ssol;
    }

    // @selected
    private String lastSimCndtnSelected;

    public String getLastSimCndtnSelected() {
        return lastSimCndtnSelected;
    }

    public void setLastSimCndtnSelected(String lastSimCndtnSelected) {
        this.lastSimCndtnSelected = lastSimCndtnSelected;
    }

    private HtmlSelectOneRadio radioSimTypeChoice = new HtmlSelectOneRadio();

    public HtmlSelectOneRadio getRadioSimTypeChoice() {
        return radioSimTypeChoice;
    }

    public void setRadioSimTypeChoice(HtmlSelectOneRadio radioSimTypeChoice) {
        this.radioSimTypeChoice = radioSimTypeChoice;
    }


    public Map<String, String> simOptionMap = new LinkedHashMap<String, String>();

    public Map<String, String> getSimOptionMap() {
        return simOptionMap;
    }

    public void setSimOptionMap(Map<String, String> simOptionMap) {
        this.simOptionMap = simOptionMap;
    }

    public String radioSimTypeChoiceSelected;

    public String getRadioSimTypeChoiceSelected() {
        return radioSimTypeChoiceSelected;
    }

    public void setRadioSimTypeChoiceSelected(String radioSimTypeChoiceSelected) {
        this.radioSimTypeChoiceSelected = radioSimTypeChoiceSelected;
    }

    // @valueChangeListener
    public void showHideSimCndtnOptPanel(ValueChangeEvent vce) {
        FacesContext cntxt = FacesContext.getCurrentInstance();

        String currentState = (String) vce.getNewValue();
        dbgLog.fine("currentState=" + currentState);
        dbgLog.fine("current model name in setx=" + getCurrentModelName());

        dbgLog.fine("within simulation-type choice: new selected="
            + radioButtonGroup1.getSelected());
        if (getCurrentModelName() != null) {

            AdvancedStatGUIdata.Model selectedModelSpec = 
                getAnalysisApplicationBean().getSpecMap()
                    .get(getCurrentModelName());
            dbgLog.fine("spec within setx:\n" + selectedModelSpec);
            if ((currentState.toString()).equals("1")) {
                //groupPanel22.setRendered(true);
                groupPanelSimNonDefault.setRendered(true);
                // set up the @items for dropDown2/dropDown3

                if (selectedModelSpec.getNoRboxes() == 2) {
                    setSetxDiffVarBox1(getAdvStatVarRBox2());
                    setSetxDiffVarBox2(getAdvStatVarRBox2());

                } else if (selectedModelSpec.getNoRboxes() == 3) {
                    setSetxDiffVarBox1(getAdvStatVarRBox3());
                    setSetxDiffVarBox2(getAdvStatVarRBox3());

                }

                cntxt.getExternalContext().getSessionMap().put(
                    "setxDiffVarBox1", setxDiffVarBox1);
                cntxt.getExternalContext().getSessionMap().put(
                    "setxDiffVarBox2", setxDiffVarBox2);
                cntxt.getExternalContext().getSessionMap().put(
                "lastSimCndtnSelected", currentState.toString());
            } else if ((currentState.toString()).equals("0")) {
                //groupPanel22.setRendered(false);
                groupPanelSimNonDefault.setRendered(false);
                cntxt.getExternalContext().getSessionMap().put(
                "lastSimCndtnSelected", currentState.toString());
            }
        } else {
            //groupPanel22.setRendered(false);
            groupPanelSimNonDefault.setRendered(false);
        }

        cntxt.renderResponse();
    }

    // simulation: option panel for the radio-button-option of select values
    // @binding
    private PanelGroup groupPanel22 = new PanelGroup();

    public PanelGroup getGroupPanel22() {
        return groupPanel22;
    }

    public void setGroupPanel22(PanelGroup pg) {
        this.groupPanel22 = pg;
    }




    private HtmlPanelGroup groupPanelSimNonDefault = new HtmlPanelGroup();

    public HtmlPanelGroup getGroupPanelSimNonDefault() {
        return groupPanelSimNonDefault;
    }

    public void setGroupPanelSimNonDefault(HtmlPanelGroup groupPanelSimNonDefault) {
        this.groupPanelSimNonDefault = groupPanelSimNonDefault;
    }




    // simulation : value for 1st diff: casing pane

    private HtmlPanelGrid gridPanel10 = new HtmlPanelGrid();

    public HtmlPanelGrid getGridPanel10() {
        return gridPanel10;
    }

    public void setGridPanel10(HtmlPanelGrid hpg) {
        this.gridPanel10 = hpg;
    }

    // simulation : value for 1st diff: label for the casing panel

    private Label label2 = new Label();

    public Label getLabel2() {
        return label2;
    }

    public void setLabel2(Label l) {
        this.label2 = l;
    }

    // simulation : value for 1st diff: pull-down var selection
    // dropdown2: ui:dropdown@binding
    private DropDown dropDown2 = new DropDown();

    public DropDown getDropDown2() {
        return dropDown2;
    }

    public void setDropDown2(DropDown dd) {
        this.dropDown2 = dd;
    }

    // dropdown2: ui:dropdown@items
    private Collection<Option> setxDiffVarBox1 = new ArrayList<Option>();

    public Collection<Option> getSetxDiffVarBox1() {
        return setxDiffVarBox1;
    }

    public void setSetxDiffVarBox1(Collection<Option> dol) {
        this.setxDiffVarBox1 = dol;
    }

    // simulation : value for 1st diff: text box
    private TextField textField8 = new TextField();

    public TextField getTextField8() {
        return textField8;
    }

    public void setTextField8(TextField tf) {
        this.textField8 = tf;
    }

    // simulation : explanatory variable value: casing panel

    private HtmlPanelGrid gridPanel11 = new HtmlPanelGrid();

    public HtmlPanelGrid getGridPanel11() {
        return gridPanel11;
    }

    public void setGridPanel11(HtmlPanelGrid hpg) {
        this.gridPanel11 = hpg;
    }

    // simulation : explanatory variable value: label for casing panel
    private Label label1 = new Label();

    public Label getLabel1() {
        return label1;
    }

    public void setLabel1(Label l) {
        this.label1 = l;
    }

    // simulation : explanatory variable value: pull-down var selection
    // dropdown3: ui:dropDown@binding
    private DropDown dropDown3 = new DropDown();

    public DropDown getDropDown3() {
        return dropDown3;
    }

    public void setDropDown3(DropDown dd) {
        this.dropDown3 = dd;
    }

    // dropdown3: ui:dropdown@items
    private Collection<Option> setxDiffVarBox2 = new ArrayList<Option>();

    public Collection<Option> getSetxDiffVarBox2() {
        return setxDiffVarBox2;
    }

    public void setSetxDiffVarBox2(Collection<Option> dol) {
        this.setxDiffVarBox2 = dol;
    }


    // simulation : explanatory variable value: text box
    private TextField textField10 = new TextField();

    public TextField getTextField10() {
        return textField10;
    }

    public void setTextField10(TextField tf) {
        this.textField10 = tf;
    }

    // submit button (modeling request)
    // advStatBttn:h:commandButton@binding
    private HtmlCommandButton advStatButton = new HtmlCommandButton();

    public HtmlCommandButton getAdvStatButton() {
        return advStatButton;
    }

    public void setAdvStatButton(HtmlCommandButton hcb) {
        this.advStatButton = hcb;
    }

    private List<Integer> getCurrentVarBoxSize(String mdlName) {
        List<Integer> bs = new ArrayList();
        int noBoxR = getAnalysisApplicationBean().getSpecMap().get(mdlName)
            .getNoRboxes();
        if (noBoxR == 1) {
            bs.add(advStatVarRBox1.size());
        } else if (noBoxR == 2) {
            bs.add(advStatVarRBox1.size());
            bs.add(advStatVarRBox2.size());
        } else if (noBoxR == 3) {
            bs.add(advStatVarRBox1.size());
            bs.add(advStatVarRBox2.size());
            bs.add(advStatVarRBox3.size());
        }
        return bs;
    }

    // checking advStat-related parameters before submission
    public boolean checkAdvStatParameters(String mdlName) {
        // boolean result=false;

        Integer noBoxR = getAnalysisApplicationBean().getSpecMap().get(mdlName)
            .getNoRboxes();
        List<Integer> RBoxSizes = getCurrentVarBoxSize(mdlName);
        dbgLog.fine("RBoxSizes=" + RBoxSizes);
        int noe = 0;
        resetMsg4MoveVar();
        for (int i = 0; i < noBoxR; i++) {

            Integer ithBoxMin = getAnalysisApplicationBean().getSpecMap().get(
                mdlName).getVarBox().get(i).getMinvar();
                
            dbgLog.fine("i-th BoxMin(" + i + ")=" + ithBoxMin);
            
            if (RBoxSizes.get(i) < ithBoxMin) {
                String ermsg = "No of Vars in Box" + i + " ("
                    + RBoxSizes.get(i) + ") is less than the minimum("
                    + ithBoxMin + ")";

                dbgLog.fine("* " + ermsg);

                noe++;
                if (i == 0) {
                    msgMoveVar1Bttn.setText("*" + ermsg);
                    msgMoveVar1Bttn.setRendered(true);
                } else if (i == 1) {
                    msgMoveVar2Bttn.setText("*" + ermsg);
                    msgMoveVar2Bttn.setRendered(true);
                } else if (i == 2) {
                    msgMoveVar3Bttn.setText("*" + ermsg);
                    msgMoveVar3Bttn.setRendered(true);
                }
            }

        }
        dbgLog.fine("noe=" + noe);
        return (noe == 0 ? true : false);
    }





    // msgAdvStatButton:ui:StaticText@binding
    private StaticText msgAdvStatButton = new StaticText();

    public StaticText getMsgAdvStatButton() {
        return msgAdvStatButton;
    }

    public void setMsgAdvStatButton(StaticText txt) {
        this.msgAdvStatButton = txt;
    }

    private String msgAdvStatButtonTxt;

    public String getMsgAdvStatButtonTxt() {
        return msgAdvStatButtonTxt;
    }

    public void setMsgAdvStatButtonTxt(String txt) {
        this.msgAdvStatButtonTxt = txt;
    }

    public void resetMsgAdvStatButton() {
        dbgLog.fine("***** within resetMsgAdvStatButton *****");
        msgAdvStatButton.setText(" ");
        FacesContext.getCurrentInstance().getExternalContext()
            .getSessionMap().put("msgAdvStatButtonTxt", msgAdvStatButtonTxt);
        msgAdvStatButton.setVisible(false);

        dbgLog.fine("***** resetMsgAdvStatButton: end  *****");
    }



    public List<String> getDataVariableForRBox1() {
        List<String> dvs = new ArrayList<String>();
        for (Iterator el = advStatVarRBox1.iterator(); el.hasNext();) {
            Option dv = (Option) el.next();
            String id = (String) dv.getValue();
            dvs.add("v" + id);
        }
        return dvs;
    }

    public List<String> getDataVariableForRBox2() {
        List<String> dvs = new ArrayList<String>();
        for (Iterator el = advStatVarRBox2.iterator(); el.hasNext();) {
            Option dv = (Option) el.next();
            String id = (String) dv.getValue();
            dvs.add("v" + id);
        }
        return dvs;
    }

    public List<String> getDataVariableForRBox3() {
        List<String> dvs = new ArrayList<String>();
        for (Iterator el = advStatVarRBox3.iterator(); el.hasNext();) {
            Option dv = (Option) el.next();
            String id = (String) dv.getValue();
            dvs.add("v" + id);
        }
        return dvs;
    }

    public List<String> getZlg(String mdlId, String mdlName) {
        List<String> ls = new ArrayList<String>();
        ls.add(mdlId + "-" + mdlName);
        return ls;
    }

    public List<String> getMdlType(String mdlName, String sf) {
        List<String> ls = new ArrayList<String>();
        dbgLog.fine("model name=" + mdlName + "  special function=" + sf);
        int typeValue = 0;

        if (mdlName.equals("blogit") || mdlName.equals("bprobit")) {
            typeValue = 1;
        } else {
            if (sf != null) {
                if (sf.equals("Surv")) {
                    typeValue = 2;
                } else {
                    String[] tmp = null;
                    tmp = mdlName.split("\\.");
                    dbgLog.fine("tmp[0]=" + tmp[0]);
                    if (tmp[0].equals("factor")) {
                        typeValue = 3;
                    }
                }

            }
        }

        dbgLog.fine("model type=" + typeValue);
        ls.add(Integer.toString(typeValue));
        return ls;
    }
    // </editor-fold>
    // end of advStat
    // <----------------------------------------------------------------------
    // </editor-fold>

    // advStatBttn:h:commandButton@action
    public String advStatAction() {
        dbgLog.fine("selected tab(advStat)="+getTabSet1().getSelected());
        // check the current model

        String mdlName = (String) dropDown1.getSelected();
        dbgLog.fine("model name=" + mdlName);
        
        AdvancedStatGUIdata.Model modelSpec = getAnalysisApplicationBean()
            .getSpecMap().get(mdlName);
        
        if (checkAdvStatParameters(mdlName)) {

            FacesContext cntxt = FacesContext.getCurrentInstance();

            HttpServletResponse res = (HttpServletResponse) cntxt
                .getExternalContext().getResponse();
            HttpServletRequest req = (HttpServletRequest) cntxt
                .getExternalContext().getRequest();

                dbgLog.fine("***** within advStatAction() *****");
                // common parts
                // data file
                StudyFile sf = dataTable.getStudyFile();
                Long noRecords = dataTable.getRecordsPerCase();

                String dsbUrl = getDsbUrl();
                dbgLog.fine("dsbUrl=" + dsbUrl);

                String serverPrefix = req.getScheme() + "://"
                    + req.getServerName() + ":" + req.getServerPort()
                    + req.getContextPath();
                    
                dbgLog.fine("serverPrefix="+serverPrefix);
//                /
//                  "optnlst_a" => "A01|A02|A03", "analysis" => "A01 A02",
//                  "varbl" => "v1.3 v1.10 v1.13 v1.22 v1.40", "charVarNoSet" =>
//                  "v1.10|v1.719",
//                 /

                // common parameters
                Map<String, List<String>> mpl = new HashMap<String, List<String>>();
                
                List<String> alst = new ArrayList<String>();
                List<String> aoplst = new ArrayList<String>();
                
                aoplst.add("A01|A02|A03");
                mpl.put("optnlst_a", aoplst);
                
                Map<String, List<String>> xtbro = new HashMap<String, List<String>>();
                
                // xtbro: modelName
                xtbro.put("modelName", Arrays.asList(mdlName));
                
                // outoput options

                List<String> outOptionList = new ArrayList<String>();
                
                mpl.put("modelName", Arrays.asList(mdlName));
                if (mdlName.equals("xtb")) {
                    mpl.put("requestType", Arrays.asList("Xtab"));
                } else {
                    mpl.put("requestType", Arrays.asList("Zelig"));
                }

                if (mdlName.equals("xtb")) {
                    alst.add("A03");
                    
                    // output options
                    // Object[] outOptn = (Object[]) checkboxGroupXtbOptions.getSelectedValue();
                    Object[] outOptn = chkbxAdvStatOutputXtbOpt.getSelectedValues();
                    List<String> tv = new ArrayList<String>();
                    //tv.add("T");
                    
                    for (int j = 0; j < outOptn.length; j++) {
                        dbgLog.fine("output option[" + j + "]=" + outOptn[j]);
                        mpl.put((String) outOptn[j], Arrays.asList("T"));
                        tv.add((String) outOptn[j]);
                    }
                        mpl.put("xtb_outputOptions",tv);
                   
                    // variables: 1st RBox
                    if (advStatVarRBox1.size() >= 1) {
                        dbgLog.fine("RB1:" + getDataVariableForRBox1());
                        mpl.put("xtb_nmBxR1", getDataVariableForRBox1());
                    }
                    
                    // variables: 2nd RBox
                    if (advStatVarRBox2.size() >= 1) {
                        dbgLog.fine("RB2:" + getDataVariableForRBox2());
                        mpl.put("xtb_nmBxR2", getDataVariableForRBox2());
                    }

                    mpl.put("analysis", alst);

                } else {
                    // Zelig cases
                    
                    dbgLog.fine("***** zelig param block *****");
                    // non-xtb, i.e., zelig cases
                    // check zlg value
                    
                    dbgLog.fine("model spec dump="+ modelSpec);
                    dbgLog.fine("model spec mdlId="+ modelSpec.getMdlId());
                        
                    String zligPrefix = modelSpec.getMdlId();
                    dbgLog.fine("model no=" + zligPrefix);
                    
                    // get the varId-list of each box
                    // 1-RBox case
                    if (advStatVarRBox1.size() >= 1) {
                        dbgLog.fine("RB1:" + getDataVariableForRBox1());
                        //mpl.put(zligPrefix + "_nmBxR1", getDataVariableForRBox1());
                        mpl.put("nmBxR1", getDataVariableForRBox1());
                    }
                    
                    // 2-RBox case
                    if (advStatVarRBox2.size() >= 1) {
                        dbgLog.fine("RB2:" + getDataVariableForRBox2());
                        //mpl.put(zligPrefix + "_nmBxR2", getDataVariableForRBox2());
                        mpl.put("nmBxR2", getDataVariableForRBox2());
                    }
                    
                    // 3-RBox case
                    if (advStatVarRBox3.size() >= 1) {
                        dbgLog.fine("RB3:" + getDataVariableForRBox3());
                        //mpl.put(zligPrefix + "_nmBxR3", getDataVariableForRBox3());
                        mpl.put("nmBxR3", getDataVariableForRBox3());
                    }
                    
                    // model name
                    //mpl.put("zlg", getZlg(zligPrefix, mdlName));
                    
                    // model type
                    //String sfn = modelSpec.getSpecialFn();
                    //mpl.put("mdlType_" + mdlName, getMdlType(mdlName, sfn));

                    // model title
                    //String ttl = modelSpec.getTitle();
                    //dbgLog.fine("model title=" + ttl);
                    //mpl.put("mdlTitle_" + mdlName, Arrays.asList(ttl));
                    //mpl.put("mdlTitle", Arrays.asList(ttl));

                    // nrBoxes
                    int noRboxes = modelSpec.getNoRboxes();
                    dbgLog.fine("noRboxes=" + noRboxes);

                    //mpl.put("noBoxes_" + mdlName, Arrays.asList(Integer.toString(noRboxes)));
                    mpl.put("noBoxes", Arrays.asList(Integer.toString(noRboxes)));

                    // binary
                    String mdlCategory = modelSpec.getCategory();
                    String outcomeType = modelSpec.getVarBox().get(0).getVarType();

                    dbgLog.fine("model category=" + mdlCategory);
                    
                    if (mdlCategory.equals("Models for Dichotomous Dependent Variables")) {
                        mpl.put("mdlDepVarType", Arrays.asList("binary"));
                    }
                    if (outcomeType.equals("binary")){
                        mpl.put("isOutcomeBinary", Arrays.asList("T"));
                    } else {
                        mpl.put("isOutcomeBinary", Arrays.asList("F"));
                    }
                    // output options
//                    //
//                     // zlg_017_Summary zlg_017_Plots zlg_017_BinOutput
//                     //
                    //Object[] outOptn = (Object[]) checkboxGroup2DefaultOptions.getSelectedValue();
                    Object[] outOptn = chkbxAdvStatOutputOpt.getSelectedValues();
                    List<String> tv = new ArrayList<String>();

                    for (int j = 0; j < outOptn.length; j++) {
                        //String outputOptnkey = zligPrefix + "_"+ (String) outOptn[j];
                        String outputOptnkey = (String) outOptn[j];
                        dbgLog.fine("zelig: output option[" + j + "]="+ outputOptnkey);
                        mpl.put(outputOptnkey, Arrays.asList("T"));
                        tv.add((String) outOptn[j]);
                    }
                    mpl.put("zelig_outputOptions",tv);
                    
                    // analysis options
//                    
//                      zlg_017_Sim zlg_017_setx zlg_017_setx_var
//                      zlg_017_setx_val_1 zlg_017_setx_val_2
//                      
//                      zlg_017_naMethod
//                     
                    //
                    if (checkbox3.isChecked()) {
                        //mpl.put(zligPrefix + "_Sim", Arrays.asList("T"));
                        mpl.put("Sim", Arrays.asList("T"));
                        
                        Object simOptn = radioButtonGroup1DefaultOptions.getSelectedValue();
                        // simOptn = 0 or 1
                        //mpl.put(zligPrefix + "_setx", Arrays.asList((String) simOptn));
                        mpl.put("setx", Arrays.asList((String) simOptn));
                        mpl.put("setxType", Arrays.asList((String) simOptn));
                        
                        if (((String) simOptn).equals("1")) {
                            Object v1 = dropDown2.getSelected();
                            Object v2 = dropDown3.getSelected();
                            Object vl1 = textField10.getValue();
                            Object vl2 = textField8.getValue();
                            
                            List<String> setxVars = new ArrayList<String>();
                            
                            if (v1 != null) {
                                setxVars.add((String) v1);

                            }
                            if (v2 != null) {
                                setxVars.add((String) v2);
                            }
                            //mpl.put(zligPrefix + "_setx_var", setxVars);
                            mpl.put("setx_var", setxVars);
                            
                            if (vl1 != null) {
                                //mpl.put(zligPrefix + "_setx_val_1", Arrays.asList((String) vl1));
                                mpl.put("setx_val_1", Arrays.asList((String) vl1));
                            }
                            if (vl2 != null) {
                                //mpl.put(zligPrefix + "_setx_val_2", Arrays.asList((String) vl2));
                                mpl.put("setx_val_2", Arrays.asList((String) vl2));
                            }


                            List<String> setxVar1 = new ArrayList<String>();
                            
                            if (v1 != null) {
                                setxVar1.add("v" + v1);
                                if (vl1 != null) {
                                    setxVar1.add((String) vl1);
                                } else {
                                    setxVar1.add("");
                                }
                                    mpl.put("setx_var1", setxVar1);
                            }
                            
                            if (v2 != null) {
                                 List<String> setxVar2 = new ArrayList<String>();
                                 
                                setxVar2.add("v"+ v2);
                                if (vl2 != null) {
                                    setxVar2.add((String) vl2);
                                } else {
                                    setxVar2.add("");
                                }
                                mpl.put("setx_var2", setxVar2);
                            }

                        }
                    } else {
                        mpl.put("Sim", Arrays.asList("F"));
                    }

                }
                
                dbgLog.fine("contents(mpl so far):" + mpl);

                // if there is a user-defined (recoded) variables
                /*
                if (recodedVarSet.size() > 0) {
                    mpl.putAll(getRecodedVarParameters());
                }
                */
//                dbgLog.fine("citation info to be sent:\n" + citation);
//                mpl.put("OfflineCitation", Arrays.asList(citation));
//                mpl.put("appSERVER", Arrays.asList(req.getServerName() +
//                    ":"+ req.getServerPort() + req.getContextPath()));

                mpl.put("studytitle", Arrays.asList(studyTitle));
                mpl.put("studyno", Arrays.asList(studyId.toString()));
                mpl.put("studyURL", Arrays.asList(studyURL));
                mpl.put("browserType", Arrays.asList(browserType));
                
                mpl.put("recodedVarIdSet", getRecodedVarIdSet());
                mpl.put("recodedVarNameSet",getRecodedVarNameSet());
                mpl.put("recodedVarLabelSet", getRecodedVarLabelSet());
                mpl.put("recodedVarTypeSet", getRecodedVariableType());
                mpl.put("recodedVarBaseTypeSet", getBaseVariableTypeForRecodedVariable());
                
                mpl.put("baseVarIdSet",getBaseVarIdSetFromRecodedVarIdSet());
                mpl.put("baseVarNameSet",getBaseVarNameSetFromRecodedVarIdSet());

            // -----------------------------------------------------
            // New processing route
            // 
            // Step 0. Locate the data file and its attributes
    
            String fileId = sf.getId().toString();
            //String fileURL = serverPrefix + "/FileDownload/?fileId=" + fileId + "&isSSR=1&xff=0&noVarHeader=1";
            String fileURL = "http://localhost:" + req.getServerPort() + "/dvn" + "/FileDownload/?fileId=" + fileId + "&isSSR=1&xff=0&noVarHeader=1";

            
            dbgLog.fine("fileURL="+fileURL);
            
            String fileloc = sf.getFileSystemLocation();
            String tabflnm = sf.getFileName();
            boolean sbstOK = sf.isSubsettable();
            String flct = sf.getFileType();
            
            dbgLog.fine("location="+fileloc);
            dbgLog.fine("filename="+tabflnm);
            dbgLog.fine("subsettable="+sbstOK);
            dbgLog.fine("filetype="+flct);

            DvnRJobRequest sro = null;

            List<File> zipFileList = new ArrayList();

            // the data file for downloading/statistical analyses must be subset-ready
            // local (relative to the application) file case 
            // note: a typical remote case is: US Census Bureau
            File tmpsbfl = null;
            if (sbstOK){
                
                try {

                // Step 1. temporarily store the whole data set in a temp directory

                    // Create a URL for the data file
                    URL url = new URL(fileURL);

                    // temp data file that stores incoming data from the above URL
                    File tmpfl = File.createTempFile("tempTabfile.", ".tab");
                    deleteTempFileList.add(tmpfl);
                    // temp subset file that stores requested variables 
                    tmpsbfl = File.createTempFile("tempsubsetfile.", ".tab");
                    deleteTempFileList.add(tmpsbfl);
                    //zipFileList.add(tmpsbfl);

                    // Typical file-copy idiom 
                    // incoming/outgoing streams
                    InputStream inb = new BufferedInputStream(url.openStream());
                    OutputStream outb = new BufferedOutputStream(new FileOutputStream(tmpfl));

                    int bufsize;
                    byte [] bffr = new byte[8192];
                    while ((bufsize = inb.read(bffr))!=-1) {
                        outb.write(bffr, 0, bufsize);
                    }
                    
                    inb.close();
                    outb.close();
                    
                    // Checks the obtained data file
                    if (tmpfl.exists()){
                        Long wholeFileSize = tmpfl.length();
                        dbgLog.fine("whole file:length="+wholeFileSize);
                        dbgLog.fine("tmp file:name="+tmpfl.getAbsolutePath());
                        
                        if (wholeFileSize <= 0){
                            // subset file exists but it is empty
                        
                            msgAdvStatButton.setText("* an data file is empty");
                            msgAdvStatButton.setVisible(true);
                            dbgLog.warning("exiting advStatAction() due to a file access error:"+
                            "a data file is empty"
                            );
                            getVDCRequestBean().setSelectedTab("tabAdvStat");

                            return "failure";
                        }
                       
                    } else {
                        // file was not created/downloaded
                        msgAdvStatButton.setText("* a data file was not created");
                        msgAdvStatButton.setVisible(true);
                        dbgLog.warning("exiting advStatAction() due to a file access error:"+
                        "a data file was not created"
                        );
                        getVDCRequestBean().setSelectedTab("tabAdvStat");

                        return "failure";
                    }                    
                    
                    // source data file: full-path name
                    String cutOp1 = tmpfl.getAbsolutePath();

                    // result(subset) data file: full-path name
                    String cutOp2 = tmpsbfl.getAbsolutePath();

                    // check whether a source file is tab-delimited or not

                    boolean fieldcut = true;
                    if ((noRecords != null) && (noRecords >=1)){
                        fieldcut = false;
                    }
                    
                    if (fieldcut){
                // Step 2.a. Set-up parameters for subsetting: cutting requested fields of data
                        // from a temp (whole) delimited file

                        Set<Integer> fields = getFieldNumbersForSubsetting();
                         dbgLog.fine("fields="+fields);

                        // Create an instance of DvnJavaFieldCutter
                        FieldCutter fc = new DvnJavaFieldCutter();

                        // Executes the subsetting request
                        fc.subsetFile(cutOp1, cutOp2, fields);

                    } else {
                // Step 2.b. Set-up parameters for subsetting: cutting requested columns of data
                        // from a temp (whole) non-delimited file
                        // Using new, native implementation of fixed-field cutting 
                        // (instead of executing rcut in a shell)

                        Map<Long, List<List<Integer>>> varMetaSet = getSubsettingMetaData(); 
                        DvnNewJavaFieldCutter fc = new DvnNewJavaFieldCutter(varMetaSet);

                        try {
                            fc.cutColumns(new File(cutOp1), varMetaSet.size(), 0, "\t", cutOp2);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();

                            msgAdvStatButton.setText("* could not generate subset due to an IO problem");
                            msgAdvStatButton.setVisible(true); 
                            dbgLog.warning("exiting advStatAction() due to an IO problem ");
                            getVDCRequestBean().setSelectedTab("tabAdvStat");

                            return "failure";

                        } catch (RuntimeException re){
                            re.printStackTrace();

                            msgAdvStatButton.setText("* could not generate subset due to an runtime error");
                            msgAdvStatButton.setVisible(true); 
                            dbgLog.warning("exiting advStatAction() due to an runtime error ");
                            getVDCRequestBean().setSelectedTab("tabAdvStat");

                            return "failure";
                        }
                        // end: non-delimited case
                    }

                    // Checks the resulting subset file 
                    if (tmpsbfl.exists()){
                        Long subsetFileSize = tmpsbfl.length();
                        dbgLog.fine("subsettFile:Length="+subsetFileSize);
                        dbgLog.fine("tmpsb file name="+tmpsbfl.getAbsolutePath());
                        
                        if (subsetFileSize > 0){
                            mpl.put("subsetFileName", Arrays.asList(cutOp2));
                            mpl.put("subsetDataFileName",Arrays.asList(tmpsbfl.getName()));
                        } else {
                            // subset file exists but it is empty
                        
                            msgAdvStatButton.setText("* an subset file is empty");
                            msgAdvStatButton.setVisible(true);
                            dbgLog.warning("exiting advStatAction() due to a subsetting error:"+
                            "a subset file is empty"
                            );
                            getVDCRequestBean().setSelectedTab("tabAdvStat");

                            return "failure";

                        }
                    } else {
                        // subset file was not created
                        msgAdvStatButton.setText("* a subset file was not created");
                        msgAdvStatButton.setVisible(true);
                        dbgLog.warning("exiting advStatAction() due to a subsetting error:"+
                        "a subset file was not created"
                        );
                        getVDCRequestBean().setSelectedTab("tabAdvStat");

                        return "failure";

                    }

                // Step 3. Organizes parameters/metadata to be sent to the implemented
                    // data-analysis-service class

                    //Map<String, Map<String, String>> vls = getValueTableForRequestedVariables(getDataVariableForRequest());
                    Map<String, Map<String, String>> vls = getValueTablesForAllRequestedVariables();

                    sro = new DvnRJobRequest(getDataVariableForRequest(), mpl, vls, recodeSchema, modelSpec);
                    
                    dbgLog.fine("Prepared sro dump:\n"+ToStringBuilder.reflectionToString(sro, ToStringStyle.MULTI_LINE_STYLE));
                    
                // Step 4. Creates an instance of the the implemented 
                    // data-analysis-service class 

                    DvnRDataAnalysisServiceImpl das = new DvnRDataAnalysisServiceImpl();

                    // Executes a request of downloading or data analysis and 
                    // capture result info as a Map <String, String>

                    resultInfo = das.execute(sro);
                    
                    
                // Step 5. Checks the DSB-exit-status code
                    if (resultInfo.get("RexecError").equals("true")){
                        //msgAdvStatButton.setText("* The Request failed due to an R-runtime error");
                        msgAdvStatButton.setText("* The Request failed due to an R-runtime error");
                        msgAdvStatButton.setVisible(true);
                        dbgLog.fine("exiting advStatAction() due to an R-runtime error");
                        getVDCRequestBean().setSelectedTab("tabAdvStat");
                        
                        return "failure";
                    } else {
                        if (recodeSchema.size()> 0){
                            resultInfo.put("subsettingCriteria",sro.getSubsetConditionsForCitation());
                        } else {
                            resultInfo.put("subsettingCriteria","");
                        }
                    }
                    
                    
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    // pass the error message to the resultPage
                    // resultInfo.put();
                    msgAdvStatButton.setText("* file URL is malformed");
                    msgAdvStatButton.setVisible(true);
                    dbgLog.warning("exiting advStatAction() due to a URL problem ");
                    getVDCRequestBean().setSelectedTab("tabAdvStat");

                     return "failure";
                     
                } catch (IOException e) {
                    // this may occur if the dataverse is not released
                    // the file exists, but it is not accessible 
                    e.printStackTrace();
                    
                    msgAdvStatButton.setText("* an IO problem occurred");
                    msgAdvStatButton.setVisible(true);
                    dbgLog.warning("exiting advStatAction() due to an IO problem ");
                    getVDCRequestBean().setSelectedTab("tabAdvStat");

                    return "failure";

                }
                // end of the subset-OK case
            } else {
                // not subsettable data file
                msgAdvStatButton.setText("* this data file is not subsettable file");
                msgAdvStatButton.setVisible(true);
                dbgLog.warning("exiting advStatAction(): the data file is not subsettable ");
                getVDCRequestBean().setSelectedTab("tabAdvStat");

                return "failure";

            } // end:subsetNotOKcase

            // final processing steps for all successful cases

            resultInfo.put("offlineCitation", citation);
            resultInfo.put("studyTitle", studyTitle);
            resultInfo.put("studyNo", studyId.toString());
            resultInfo.put("studyURL", studyURL);
            resultInfo.put("R_min_verion_no","2.7.0");
            resultInfo.put("dataverse_version_no","1.3");
            
            dbgLog.fine("RwrkspFileName="+resultInfo.get("wrkspFileName"));

            // writing necessary files
            try{

                // rename the subsetting file
                File tmpsbflnew = File.createTempFile(SUBSET_FILENAME_PREFIX + resultInfo.get("PID") +".", ".tab");
                deleteTempFileList.add(tmpsbflnew);
                InputStream inb = new BufferedInputStream(new FileInputStream(tmpsbfl));
                OutputStream outb = new BufferedOutputStream(new FileOutputStream(tmpsbflnew));

                int bufsize;
                byte [] bffr = new byte[8192];
                while ((bufsize = inb.read(bffr))!=-1) {
                    outb.write(bffr, 0, bufsize);
                }
                inb.close();
                outb.close();
                
                String rhistNew = StringUtils.replace(resultInfo.get("RCommandHistory"), tmpsbfl.getName(),tmpsbflnew.getName());
                
                //zipFileList.add(tmpsbflnew);

                // (1) write a citation file 
//                String citationFilePrefix = "citationFile_"+ resultInfo.get("PID") + "_";
//                File tmpcfl = File.createTempFile(citationFilePrefix, ".txt");
//
//                zipFileList.add(tmpcfl);
//                deleteTempFileList.add(tmpcfl);
//
//                DvnCitationFileWriter dcfw = new DvnCitationFileWriter(resultInfo);
//
//                String fmpcflFullname = tmpcfl.getAbsolutePath();
//                String fmpcflname = tmpcfl.getName();
//                dcfw.write(tmpcfl);

                // (2) R command file
                String rhistoryFilePrefix = "RcommandFile_" + resultInfo.get("PID") + "_";
                File tmpRhfl = File.createTempFile(rhistoryFilePrefix, ".R");

                zipFileList.add(tmpRhfl);
                deleteTempFileList.add(tmpRhfl);
                resultInfo.put("dvn_R_helper_file","dvn_helper.R");
                DvnReplicationCodeFileWriter rcfw = new DvnReplicationCodeFileWriter(resultInfo);
                if (mdlName.equals("xtb")){
                    rcfw.writeXtabCode(tmpRhfl);
                } else {
                    rcfw.writeZeligCode(tmpRhfl);
                }
                
                // (3) RData Replication file
                String wrkspFileName = resultInfo.get("wrkspFileName");
                dbgLog.fine("wrkspFileName="+wrkspFileName);
                
                File RwrkspFileName = new File(wrkspFileName);
                if (RwrkspFileName.exists()){
                    dbgLog.fine("RwrkspFileName:length="+RwrkspFileName.length());

                    zipFileList.add(RwrkspFileName);

                } else {
                    dbgLog.fine("RwrkspFileName does not exist");
                    //msgAdvStatButton.setText("* The workspace file is not available");
                    //msgAdvStatButton.setVisible(true);
                    dbgLog.warning("advStatAction(): R workspace file was not transferred");
                    //getVDCRequestBean().setSelectedTab("tabAdvStat");

                    //return "failure";
                }
                deleteTempFileList.add(RwrkspFileName);

                // vdc_startup.R file
//                String vdc_startupFileName = resultInfo.get("vdc_startupFileName");
//                dbgLog.fine("vdc_startupFileName="+vdc_startupFileName);
//                File vdcstrtFileName = new File(vdc_startupFileName);
//                if (vdcstrtFileName.exists()){
//                    dbgLog.fine("vdcstrtFileName:length="+vdcstrtFileName.length());
//                    zipFileList.add(vdcstrtFileName);
//                } else {
//                    dbgLog.fine("vdcstrtFileName does not exist");
//                    //msgAdvStatButton.setText("* vdc_startup.R is not available");
//                    //msgAdvStatButton.setVisible(true);
//                    dbgLog.warning("advStatAction(): vdc_startup.R was not transferred");
//                    //getVDCRequestBean().setSelectedTab("tabAdvStat");
//
//                    //return "failure";
//                }
//                deleteTempFileList.add(vdcstrtFileName);
                // (4) add replication readme file
                
                File readMeFile = File.createTempFile(REP_README_FILE_PREFIX + resultInfo.get("PID") +"_", ".txt");
                DvnReplicationREADMEFileWriter rw = new DvnReplicationREADMEFileWriter(resultInfo);
                rw.writeREADMEfile(readMeFile);
                //zipFileList.add(REP_README_FILE);
                zipFileList.add(readMeFile);
                deleteTempFileList.add(readMeFile);
                // (5) helper
                zipFileList.add(DVN_R_HELPER_FILE);
                if (mdlName.equals("xtb")){
                    // (6) css file
                    zipFileList.add(DVN_R2HTML_CSS_FILE);
                   
                }
                // zip the following files as a replication-pack
                //
                // local     local        local      remote
                // citation, tab-file,   R history,  wrksp

                for (File f : zipFileList){
                    dbgLog.fine("path="+f.getAbsolutePath() +"\tname="+ f.getName());
                }


                // zipping all required files
                try{
                    String zipFilePrefix = "zipFile_" + resultInfo.get("PID") + "_";
                    File zipFile  = File.createTempFile(zipFilePrefix, ".zip");
                    deleteTempFileList.add(zipFile);
                    //res.setContentType("application/zip");
                    String zfname = zipFile.getAbsolutePath();
                    //res.setHeader("content-disposition", "attachment; filename=" + zfname);
                    
                    OutputStream zfout = new FileOutputStream(zipFile);
                    //zipFiles(res.getOutputStream(), zipFileList);
                    zipFiles(zfout, zipFileList);
                    
                    if (zipFile.exists()){
                        Long zipFileSize = zipFile.length();
                        dbgLog.fine("zip file:length="+zipFileSize);
                        dbgLog.fine("zip file:name="+zipFile.getAbsolutePath());
                        if (zipFileSize > 0){
                            resultInfo.put("replicationZipFile", zfname);
                            resultInfo.put("replicationZipFileName", zipFile.getName());
                            
                        } else {
                            dbgLog.fine("zip file is empty");
                        }
                    } else {
                        dbgLog.fine("zip file was not saved");
                    }
                                        
                    // put resultInfo into the session object
                    resultInfo.remove("RCommandHistory");
                    FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(
                        "resultInfo", resultInfo);
                        
                    dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                        
                    dbgLog.fine("***** within advStatAction(): succcessfully ends here *****");
                        
                    return "success";
                
                } catch (IOException e){
                    // file-access problem, etc.
                    e.printStackTrace();
                    dbgLog.fine("zipping IO exception");
                    msgAdvStatButton.setText("* an IO problem occurred during zipping replication files");
                    msgAdvStatButton.setVisible(true);
                    dbgLog.warning("exiting edaAction() due to an zipping IO problem ");
                    //getVDCRequestBean().setSelectedTab("tabAdvStat");
                    dvnDSBTimerService.createTimer(deleteTempFileList, TEMP_FILE_LIFETIME);
                    return "success";
                }
                // end of zipping step

            } catch (IOException e){
                // io errors caught during writing files
                e.printStackTrace();
                
                msgAdvStatButton.setText("* an IO problem occurred");
                msgAdvStatButton.setVisible(true);
                dbgLog.warning("exiting edaAction() due to an IO problem ");
                getVDCRequestBean().setSelectedTab("tabAdvStat");

                return "failure";
            }
            
            
            
            
            
            
            
            // end of CheckParameters: OK case
        } else {
            // parameters are not complete: show error message;

            msgAdvStatButton.setText("* Selection is incomplete");
            msgAdvStatButton.setVisible(true);
            dbgLog.fine("exiting advStatAction(): selection is incomplete");
            getVDCRequestBean().setSelectedTab("tabAdvStat");

            return "failure";
        }
        
    }    
    
    // -----------------------------------------------------------------------
    // subsetting-instruction section
    // -----------------------------------------------------------------------
    // <editor-fold desc="subsetting instruction">

    // message block: subsetting allowed
    private HtmlOutputText txtSubsettingInstruction = new HtmlOutputText();

    public HtmlOutputText getTxtSubsettingInstruction() {
        return txtSubsettingInstruction;
    }

    public void setTxtSubsettingInstruction(HtmlOutputText hot) {
        this.txtSubsettingInstruction = hot;
    }

    // message block: subsetting not allowed
    private HtmlOutputText txtNonSubsettingInstruction = new HtmlOutputText();

    public HtmlOutputText getTxtNonSubsettingInstruction() {
        return txtNonSubsettingInstruction;
    }

    public void setTxtNonSubsettingInstruction(HtmlOutputText hot) {
        this.txtNonSubsettingInstruction = hot;
    }


    private PanelGroup wrapSubsettingInstruction = new PanelGroup();
    /**
     * Getter for component wrapSubsettingInstruction
     *
     * @return    ui:panelGroup of the pane 
     */
    public PanelGroup getWrapSubsettingInstruction() {
        return wrapSubsettingInstruction;
    }
    /**
     * Setter for panelGroup wrapSubsettingInstruction
     *
     * @param pg    ui:panelGroup of the pane
     */
    public void setWrapSubsettingInstruction(PanelGroup pg) {
        wrapSubsettingInstruction = pg;
    }

    private Boolean wrapSubsettingInstructionRendered;

    /**
     * Getter for attribute wrapSubsettingInstructionRendered
     *
     * @return    the rendered attribute of ui:panelGroup
     */    
    public Boolean getWrapSubsettingInstructionRendered(){
        return wrapSubsettingInstructionRendered;
    }

    /**
     * Setter for attribute wrapSubsettingInstructionRendered
     *
     * @param rndrd    the rendered attribute of ui:panelGroup
     */
    public void setWrapSusettingInstructionRendered(Boolean rndrd){
        wrapSubsettingInstructionRendered = rndrd;
    }


    private PanelGroup wrapNonSubsettingInstruction = new PanelGroup();
    /**
     * Getter for component wrapNonSubsettingInstruction
     *
     * @return    ui:panelGroup of the pane 
     */
    public PanelGroup getWrapNonSubsettingInstruction() {
        return wrapSubsettingInstruction;
    }
    /**
     * Setter for panelGroup wrapNonSubsettingInstruction
     *
     * @param pg    ui:panelGroup of the pane
     */
    public void setWrapNonSubsettingInstruction(PanelGroup pg) {
        wrapNonSubsettingInstruction = pg;
    }

    private Boolean wrapNonSubsettingInstructionRendered;

    /**
     * Getter for attribute wrapNonSubsettingInstructionRendered
     *
     * @return    the rendered attribute of ui:panelGroup
     */    
    public Boolean getWrapNonSubsettingInstructionRendered(){
        return wrapNonSubsettingInstructionRendered;
    }

    /**
     * Setter for attribute wrapNonSubsettingInstructionRendered
     *
     * @param rndrd    the rendered attribute of ui:panelGroup
     */
    public void setWrapNonSubsettingInstructionRendered(Boolean rndrd){
        wrapNonSubsettingInstructionRendered = rndrd;
    }
    
    
    
    // end of subsetting-instruction
    // <----------------------------------------------------------------------
    // </editor-fold>

    // -----------------------------------------------------------------------
    // How-many-variable-DropDown menu
    // -----------------------------------------------------------------------
    // <editor-fold desc="How Many Variable Menu">

    private HtmlInputText textField4 = new HtmlInputText();

    public HtmlInputText getTextField4() {
        return textField4;
    }

    public void setTextField4(HtmlInputText hit) {
        this.textField4 = hit;
    }

    // ui:drowpDown: howManyRows
    // howManyRows@binding

    private DropDown howManyRows = new DropDown();

    public DropDown getHowManyRows() {
        return howManyRows;
    }

    public void setHowManyRows(DropDown dd) {
        this.howManyRows = dd;
    }

    // howManyRows@items
    private SingleSelectOptionsList howManyRowsOptions = new SingleSelectOptionsList();

    public SingleSelectOptionsList getHowManyRowsOptions() {
        return howManyRowsOptions;
    }

    public void setHowManyRowsOptions(SingleSelectOptionsList ssol) {
        this.howManyRowsOptions = ssol;
    }

    // howManyRows@valueChangeListener

    public void howManyRows_processValueChange(ValueChangeEvent vce) {
        dbgLog.fine("***** howManyRows_processValueChange: start *****");
        // the value of show-all-rows option == 0
        dbgLog.fine("old number of Rows=" + vce.getOldValue());
        if (vce.getOldValue()==null){
            return;
        }
        dbgLog.fine("new number of Rows=" + vce.getNewValue());
        dbgLog.fine("current Row Index(1)=" + data.getRowIndex());
        selectedNoRows = (String) howManyRows.getSelected();
        dbgLog.fine("selected number of Rows=" + selectedNoRows);

        int newNoRows = Integer.parseInt(selectedNoRows);
        if (newNoRows == 0) {
            newNoRows = data.getRowCount();
        }
        dbgLog.fine("acutually selected number of Rows=" + newNoRows);
        data.setRows(newNoRows);
        dbgLog.fine("first row to be shown=" + data.getFirst());
        dbgLog.fine("current Row Index(2)=" + data.getRowIndex());
        FacesContext.getCurrentInstance().renderResponse();
        howManyVarsChecked();
        FacesContext.getCurrentInstance().getExternalContext()
            .getSessionMap().put("selectedNoRows", selectedNoRows);

        dbgLog.fine("***** howManyRows_processValueChange: end *****");
    }

    private String howManyRowsClientId;

    public String getHowManyRowsClientId() {
        FacesContext cntxt = FacesContext.getCurrentInstance();
        return howManyRows.getClientId(cntxt);
    }

    private String selectedNoRows;

    // </editor-fold>
    
    // -----------------------------------------------------------------------
    // variable Table section
    // -----------------------------------------------------------------------
    // <editor-fold desc="variable table">

    // Variable Table: @binding

    /**
     * UIData that backs the binding attribute of 
     * the variable table (h:dataTable) whose id is dataTable1
     *
     */
    private UIData data = null;

    /**
     * Getter for property data
     *
     * @return     property data
     */
    public UIData getData() {
        return data;
    }
    /**
     * Setter for property data
     *
     * @param data new UIData
     */
    public void setData(UIData data) {
        this.data = data;
    }

    // -----------------------------------------------------------------------
    // scroller-related methods
    // -----------------------------------------------------------------------
    /**
     * Shifts the displayed rows of the variable table back to the top
     *
     * @return    null String
     */
    public String first() {
        scroll(0);
        return (null);
    }

    /**
     * Shifts the displayed rows of the variable table forward to the buttom
     *
     * @return    null String
     */
    public String last() {
        scroll(data.getRowCount() - 1);
        return (null);
    }

    /**
     * Shifts the displayed rows of the variable table one page forward
     *
     * @return    null String
     */
    public String next() {
        int first = data.getFirst();
        scroll(first + data.getRows());
        return (null);
    }

    /**
     * Shifts the displayed rows of the variable table one page back
     *
     * @return    null String
     */
    public String previous() {
        int first = data.getFirst();
        scroll(first - data.getRows());
        return (null);
    }

    /**
     * Shifts the displayed rows of the variable table to
     * the indexed position on which an end-user clicked
     *
     * @param row    the new current row Id number (integer)
     */

    public void scroll(int row) {
        int rows = data.getRows();
        dbgLog.fine("within scroll:rows=" + rows);
        if (rows < 1) {
            return;
        }
        if (rows < 0) {
            data.setFirst(0);
        } else if (rows >= data.getRowCount()) {
            data.setFirst(data.getRowCount() - 1);
        } else {
            data.setFirst(row - (row % rows));
        }
    }

    /**
     * Shifts the displayed rows of the variable table upon an end-user's
     * clicking action on the scroller element
     *
     * @param event ActionEvent event
     */
    public void processScrollEvent(ActionEvent event) {
        int currentRow = 1;
        UIComponent component = event.getComponent();
        Integer curRow = (Integer) component.getAttributes().get("currentRow");
        dbgLog.fine("within processScrollEvent: curRow=" + curRow);
        int firstRow = data.getFirst();
        dbgLog.fine("b: 1st row-index value=" + firstRow);
        int lastRow = data.getFirst() + data.getRows();
        dbgLog.fine("b: tentative last row-index value=" + lastRow);
        if (curRow != null) {
            currentRow = curRow.intValue();
        }
        dbgLog.fine("currentRow=" + currentRow);
        scroll(currentRow);
        firstRow = data.getFirst();
        dbgLog.fine("a: 1st row-index value=" + firstRow);
        lastRow = data.getFirst() + data.getRows();
        dbgLog.fine("a: tentative last row-index value=" + lastRow);
        howManyVarsChecked();
    }

    // -----------------------------------------------------------------------
    // select-all checkbox
    // -----------------------------------------------------------------------
    
    /**
     * The Checkbox object backing the binding attribute of ui:checkbox 
     * component that renders the select/unselect-all variable checkbox
     *  in the first column of the header of the variable table
     */
    private Checkbox checkboxSelectUnselectAll = new Checkbox();
    
    /**
     * Getter for component checkboxSelectUnselectAll
     *
     * @return  the Checkbox object backing the
     *          select/unselect-all variable checkbox
     */
    public Checkbox getCheckboxSelectUnselectAll() {
        return checkboxSelectUnselectAll;
    }
    
    /**
     * Setter for component checkboxSelectUnselectAll
     *
     * @param c    the Checkbox object that backs the
     *             select/unselect-all variable checkbox
     */
    public void setCheckboxSelectUnselectAll(Checkbox c) {
        checkboxSelectUnselectAll = c;
    }

    /**
     * To avoid serialization-related errors, the component-binding of
     * checkboxSelectUnselectAll should not be used for state-keeping
     * The two attributes of this checkbox object, 
     * selected and rendered, must be separately handled by two new 
     * Boolean properties.
     * attribute       Boolean property
     * --.selected     checkboxSelectUnselectAllSelected
     * --.rendered     checkboxSelectUnselectAllRendered
     */

    /**
     * The Boolean object backing the selected attribute of
     * checkboxSelectUnselectAll 
     */
    private Boolean checkboxSelectUnselectAllSelected;
    
    /**
     * Getter for property checkboxSelectUnselectAllSelected
     *
     * @return    the selected attribute of checkboxSelectUnselectAll
     */
    public Boolean getCheckboxSelectUnselectAllSelected(){
        return checkboxSelectUnselectAllSelected;
    }
    /**
     * Setter for property checkboxSelectUnselectAllSelected
     *
     * @param     The Boolean object that backs the selected 
     *            attribute of checkboxSelectUnselectAll
     */
    public void setCheckboxSelectUnselectAllSelected(Boolean c){
        checkboxSelectUnselectAllSelected=c;
    }
    
    /**
     * The Boolean object backing the rendered attribute of
     * checkboxSelectUnselectAll 
     */
    private Boolean checkboxSelectUnselectAllRendered;

    /**
     * Getter for property checkboxSelectUnselectAllRendered
     *
     * @return    the rendered attribute of checkboxSelectUnselectAll
     */
    public Boolean getCheckboxSelectUnselectAllRendered(){
        return checkboxSelectUnselectAllRendered;
    }
    
    /**
     * Setter for property checkboxSelectUnselectAllRendered
     *
     * @param     The Boolean object that backs the rendered 
     *            attribute of checkboxSelectUnselectAll
     */
    public void setCheckboxSelectUnselectAllRendered(Boolean c){
        checkboxSelectUnselectAllRendered = c;
    }

    /**
     * Updates the Boolean state of the select/unselect-all checkbox 
     * via its backing attribute checkboxSelectUnselectAllSelected and
     * saves this Boolean state in the sessionMap object
     */
    public void howManyVarsChecked() {
        dbgLog.fine("***** howManyVarsChecked: start *****");
        // get the 1st and last ones of the displayed rows
        int firstRow = data.getFirst();
        dbgLog.fine("1st row-index value=" + firstRow);
        int lastRow = data.getFirst() + data.getRows();
        dbgLog.fine("tentative last row-index value=" + lastRow);
        int remain = data.getRowCount() - firstRow;
        if (remain < data.getRows()) {
            lastRow = data.getFirst() + remain;
            dbgLog.fine("adjusted last row-index value=" + lastRow);
        }
        dbgLog.fine("how many rows are displayed=" + data.getRows());
        
        int counter = 0;
        for (int i = firstRow; i < lastRow; i++) {
            List<Object> rw = new ArrayList<Object>();
            rw = (ArrayList) dt4Display.get(i);
            if ((Boolean) rw.get(0)) {
                counter++;
            }
        }
        int diff = lastRow - firstRow;
        dbgLog.fine("how many rows to be displayed=" + diff);
        dbgLog.fine("how many rows are checked=" + counter);
        if (counter == diff) {
            // check the checkbox
            checkboxSelectUnselectAll.setSelected(Boolean.TRUE);
            dbgLog.fine("set the select/unselect-all checkbox checked");
        } else {
            // uncheck the checkbox
            checkboxSelectUnselectAll.setSelected(Boolean.FALSE);
            dbgLog.fine("set the select/unselect-all checkbox UN-checked");
        }
        
        // Stores the objects that represents the properties of the component
        // instead of the component itsefl in the session map
        /* deprecated
            FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("checkboxSelectUnselectAll",
                checkboxSelectUnselectAll);
        */
            checkboxSelectUnselectAllSelected = 
                (Boolean)checkboxSelectUnselectAll.getSelected();
            FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("checkboxSelectUnselectAllSelected",
                checkboxSelectUnselectAllSelected);
        dbgLog.fine("***** howManyVarsChecked: end *****");
    }
    
    /**
     * Updates backing objects and components after an end-user 
     * checked/unchecked the checkbox checkboxSelectUnselectAll.
     * Attached to the valueChangeListener  attribute of ui:checkbox
     * component whose id is checkboxSelectUnselectAll
     *
     * @param vce ValueChangeEvent event 
     */
    public void selectUnselectAllCheckbox(ValueChangeEvent vce) {
        dbgLog.fine("***** selectUnselectAllCheckbox: start *****");

        // toggle false to true or vice versa
        FacesContext cntxt = FacesContext.getCurrentInstance();
        Boolean oldState = (Boolean) vce.getOldValue();
        dbgLog.fine("oldState=" + oldState);
        Boolean currentState = (Boolean) vce.getNewValue();
        dbgLog.fine("newState=" + currentState);

        // clear the error message if it still exists
        resetMsgVariableSelection();

        // check the displayed rows
        int firstRow = data.getFirst();
        dbgLog.fine("1st-row index value=" + firstRow);
        int lastRow = data.getFirst() + data.getRows();
        dbgLog.fine("tentative last row-index value=" + lastRow);
        int remain = data.getRowCount() - firstRow;
        if (remain < data.getRows()) {
            lastRow = data.getFirst() + remain;
            dbgLog.fine("adjusted last row-index value=" + lastRow);
        }
        dbgLog.fine("how many rows are to be displayed=" + data.getRows());

        Set<String> bvIdSet = new HashSet<String>();
        Set<String> rmIdSet = new HashSet<String>();
        int bvcnt = 0;
        for (int i = firstRow; i < lastRow; i++) {
            List<Object> rw = new ArrayList<Object>();
            rw = (ArrayList) dt4Display.get(i);
            String varId = (String) rw.get(2);

            // check this var is a base-var for recoded vars
            if (!baseVarToDerivedVar.containsKey(varId)) {
                rw.set(0, currentState);
                rmIdSet.add(varId);
            } else {
                // keep this var's checkbox checked
                rw.set(0, Boolean.TRUE);
                bvcnt++;
                bvIdSet.add(varId);
            }
            dt4Display.set(i, rw);
        }
        dbgLog.fine("conents:bvIdSet=" + bvIdSet);
        dbgLog.fine("number of recoded vars=" + bvcnt);

        if (currentState) {
            // select-all case
            dbgLog.fine("select all case: add all variable to varCart, etc.");

            for (int i = firstRow; i < lastRow; i++) {
                String keyS = (String) ((ArrayList) dt4Display.get(i)).get(2);
                String valueS = (String) ((ArrayList) dt4Display.get(i)).get(3);
                // Long newKey= Long.valueOf(keyS);
                // if (!varCart.containsKey(newKey)){
                if (!varCart.containsKey(keyS)) {
                    // varCart.put( newKey, valueS );
                    varCart.put(keyS, valueS);
                    getVarSetAdvStat().add(new Option(keyS, valueS));
                }
            }
            // activate buttons
            activateButtons();
            checkboxSelectUnselectAll.setSelected(Boolean.TRUE);
        } else {
            // unselect-all case
            dbgLog.fine("unselect all case");
            if (bvcnt == 0) {
                // no recoded var case
                dbgLog.fine("un-select-all case: no recoding vars");
                // backing Map object
                varCart.clear();
                // LHS listbox
                varSetAdvStat.clear();
                // RHS listboxes
                advStatVarRBox1.clear();
                advStatVarRBox2.clear();
                advStatVarRBox3.clear();

                // RHS simulation option: unchecked
                checkbox3.setSelected(false);

                // simulation-type radio button group
                radioButtonGroup1.setSelected("0");
                groupPanel20.setRendered(false);
                groupPanelSimTypeChoice.setRendered(false);
                //groupPanel22.setRendered(false);
                groupPanelSimNonDefault.setRendered(false);
                // deactivate buttons
                deactivateButtons();
                // clear the recodeTable area
                clearRecodeTargetVarInfo();
                // hide recode Area
                hideRecodeTableArea();
            } else {
                // at least one recoded variable exists
                dbgLog.fine("un-select-all case: "+
                "some variables are used for recoding and they are kept");
                
                // clear varCart(Map) except for base vars for recoding
                for (String v : rmIdSet) {
                    varCart.remove(v);
                }

                dbgLog.fine("pass the block for varCart");
                // clear varSetAdvStat except for base vars for recoding
                // LHS listbox object (Collection ArrayList<Option>)
                Collection<Option> tmpvs = new ArrayList<Option>();
                for (Iterator i = varSetAdvStat.iterator(); i.hasNext();) {
                    Option el = (Option) i.next();
                    if (bvIdSet.contains((String) el.getValue())) {
                        tmpvs.add(new Option(el.getValue(), el.getLabel()));
                    }
                }
                dbgLog.fine("contents:tmpvs=" + tmpvs);
                varSetAdvStat.clear();
                varSetAdvStat.addAll(tmpvs);

                dbgLog.fine("pass the block for varSetAdvState");

                // RHS listbox1
                Collection<Option> tmpRBox1 = new ArrayList<Option>();
                for (Iterator i = advStatVarRBox1.iterator(); i.hasNext();) {
                    Option el = (Option) i.next();
                    if (!bvIdSet.contains((String) el.getValue())) {
                        tmpRBox1.add(new Option(el.getValue(), el.getLabel()));

                    }
                }
                advStatVarRBox1.clear();
                advStatVarRBox1.addAll(tmpRBox1);
                dbgLog.fine("pass the block for advStatVarRBox1");

                // RHS listbox2
                Collection<Option> tmpRBox2 = new ArrayList<Option>();
                for (Iterator i = advStatVarRBox2.iterator(); i.hasNext();) {
                    Option el = (Option) i.next();
                    if (bvIdSet.contains((String) el.getValue())) {
                        tmpRBox2.add(new Option(el.getValue(), el.getLabel()));
                    }
                }
                advStatVarRBox2.clear();
                advStatVarRBox2.addAll(tmpRBox2);

                // RHS listbox3
                Collection<Option> tmpRBox3 = new ArrayList<Option>();
                for (Iterator i = advStatVarRBox3.iterator(); i.hasNext();) {
                    Option el = (Option) i.next();
                    if (bvIdSet.contains((String) el.getValue())) {
                        tmpRBox3.add(new Option(el.getValue(), el.getLabel()));
                    }
                }
                advStatVarRBox3.clear();
                advStatVarRBox3.addAll(tmpRBox3);

                msgVariableSelection.setRendered(true);
                msgVariableSelection.setText(
                    "At least one variable is used for recoding;<br />"+
                    "Remove its recoded variable(s) first.");
            }
        }
        /* deprecated: not component but backing object is
                       saved in the sessionMap
        cntxt.getExternalContext().getSessionMap()
            .put("checkboxSelectUnselectAll", checkboxSelectUnselectAll);
        */
        // Saves the Boolean backing object in the sessionMap
        checkboxSelectUnselectAllSelected = 
            (Boolean)checkboxSelectUnselectAll.getSelected();
        cntxt.getExternalContext().getSessionMap()
            .put("checkboxSelectUnselectAllSelected",
            checkboxSelectUnselectAllSelected);
        cntxt.renderResponse();
        dbgLog.fine("***** selectUnselectAllCheckbox: end *****");
    }

    // -----------------------------------------------------------------------
    // variable-table: Checkboxes in the first column
    // -----------------------------------------------------------------------

    private HtmlSelectBooleanCheckbox varCheckboxx = new HtmlSelectBooleanCheckbox();
    
    public HtmlSelectBooleanCheckbox getVarCheckboxx() {
        return varCheckboxx;
    }

    public void setVarCheckboxx(HtmlSelectBooleanCheckbox varCheckboxx) {
        this.varCheckboxx = varCheckboxx;
    }

    /**
     * ui:checkbox component backing the binding attribute of a checkbox
     * in the first column of the variable table in the subsetting page and
     * whose id is varCheckbox
     */
//    private Checkbox varCheckbox = new Checkbox();
    
    /**
     * Getter for component varCheckbox
     *
     * @return    Checkbox object
     */
    
//    public Checkbox getVarCheckbox() {
//        return varCheckbox;
//    }
    
    /**
     * Setter for component varCheckbox 
     *
     * @param c    Checkbox object
     */
//    public void setVarCheckbox(Checkbox c) {
//        this.varCheckbox = c;
//    }

    /**
     * Updates backing objects and components after
     * an end-user checked/unchecked a checkbox in each of the variable table.
     * Attached to the valueChangeListener attribute of ui:checkbox
     * component whose id is varCheckbox
     *
     * @param vce    ValueChangeEvent event
     */
    public void updateCheckBoxState(ValueChangeEvent vce) {
        int cr = 0;

        List<Object> tmpDataLine = (List<Object>) data.getRowData();
        dbgLog.fine("current varName=" + tmpDataLine.get(3));
        dbgLog.fine("current varId=" + tmpDataLine.get(2));
        String varId = (String) tmpDataLine.get(2);

        resetMsgVariableSelection();

        // remove vars from RHS boxes
        resetMsgDwnldButton();
        resetMsgSaveRecodeBttn();
        resetMsgEdaButton();
        resetMsg4MoveVar();
        resetMsgAdvStatButton();



        if ((Boolean) tmpDataLine.get(0)) {
            dbgLog.fine("row Id=" + tmpDataLine.get(2)
                + ":current checkbox value is [true]");
        } else {
            dbgLog.fine("row Id=" + tmpDataLine.get(2)
                + ":current checkbox value is [false]");
        }

        Boolean currentState = (Boolean) vce.getNewValue();

        // update the state of the selected value of this row

        tmpDataLine.set(0, currentState);
        if (currentState) {
            // put
            // varCart.put(dt4Display.get(cr).getVarId(),
            // dt4Display.get(cr).getVarName());
            // ID and Variable Name
            // varCart.put(Long.valueOf((String) tmpDataLine.get(2)), (String)
            // tmpDataLine.get(3));
            varCart.put((String) tmpDataLine.get(2), (String) tmpDataLine
                .get(3));

            getVarSetAdvStat().add(
                new Option((String) tmpDataLine.get(2), (String) tmpDataLine
                    .get(3)));
            
        } else {
            // remove

            // varCart.remove(Long.valueOf((String) tmpDataLine.get(2)));
            // check this var is used for recoded var

            if (baseVarToDerivedVar.containsKey(varId)) {
                // Case: Used for recoding
                dbgLog.fine("this var is already used for recoding and cannot be unchekced");
                // flips back the checkbox [checked]
                tmpDataLine.set(0, Boolean.TRUE);
                dbgLog.fine("flip the boolean value");
                // shows the error message
                msgVariableSelection.setRendered(true);
                msgVariableSelection
                    .setText("The variable ("
                        + tmpDataLine.get(3)
                        + ") is used for recoding;<br />Remove its recoded variable(s) first.");

                FacesContext.getCurrentInstance().renderResponse();
            } else {
                // Case: Not used for recoding
                // Removes this variable
                varCart.remove(varId);
                
                // Removes this variable from a variable box
                if (removeOption((String) tmpDataLine.get(2),
                    getVarSetAdvStat()) == null) {
                    // Rbox1
                    if (removeOption((String) tmpDataLine.get(2),
                        getAdvStatVarRBox1()) == null) {
                        // Rbox2
                        if (removeOption((String) tmpDataLine.get(2),
                            getAdvStatVarRBox2()) == null) {
                            // Rbox3
                            if (removeOption((String) tmpDataLine.get(2),
                                getAdvStatVarRBox3()) == null) {
                                dbgLog.fine("Unchecked var is not found in these boxes");
                            }
                        }
                    }
                }
                dbgLog.fine("recoded var is=" + getCurrentRecodeVariableId());
            }

        }
        // Checkes the number of the currently selected variables and 
        // if positive, enable command buttons; disable them otherwise
        if (!varCart.isEmpty()) {
            // enable buttons
            activateButtons();
        } else {
            // disable buttons
            deactivateButtons();
            // reset recode field
        }
    }
    
    /**
     * ui:staticText component that is under the subsetting instruction
     * to show an error message when a base-variable for a recoded variable
     * is unchecked.  Exposed to the subsetting page and 
     * the component id is the same as this property
     */
    private StaticText msgVariableSelection = new StaticText();
    
    /**
     * Getter for property msgVariableSelection
     *
     * @return    an unchecked-error message 
     */
    public StaticText getMsgVariableSelection() {
        return msgVariableSelection;
    }

    /**
     * Setter for property msgVariableSelection
     *
     * @param txt    an uncheck-error message to be displayed
     */
    public void setMsgVariableSelection(StaticText txt) {
        this.msgVariableSelection = txt;
    }
    
    /**
     * Rests message components that display a wrong-variable-selection 
     * error messages
     */
    public void resetMsgVariableSelection() {
        dbgLog.fine("***** resetMsgVariableSelection: start *****");
        msgVariableSelection.setRendered(false);
        msgVariableSelection.setText(" ");
        dbgLog.fine("***** resetMsgVariableSelection: end *****");
    }
    
    /**
     * Enables command buttons in the subsetting page after 
     * at least one variable selected by an end-user
     */
    public void activateButtons() {
        dwnldButton.setDisabled(false);
        recodeButton.setDisabled(false);
        moveRecodeVarBttn.setDisabled(false);
        edaButton.setDisabled(false);
        advStatButton.setDisabled(false);
    }

    /**
     * Disables command buttons in the subsetting page until 
     * at least one variable is selected by an end-user
     */
    public void deactivateButtons() {
        dwnldButton.setDisabled(true);
        recodeButton.setDisabled(true);
        moveRecodeVarBttn.setDisabled(true);
        edaButton.setDisabled(true);
        advStatButton.setDisabled(true);
    }



    // -----------------------------------------------------------------------
    // Summary Statistics
    // -----------------------------------------------------------------------

    /**
     * A Collection of summary statistics of each variable in the
     * requested data file.  Used in the recoding-GUI rendering process
     */
    private Collection<SummaryStatistic> summaryStatistics;

    /**
     * Getter for property summaryStatistics
     * 
     * @return    a Collection of summaryStatistic objects
     */
    public Collection<SummaryStatistic> getSummaryStatistics() {
        return this.summaryStatistics;
    }

    /**
     * Setter for property summaryStatistics
     * 
     * @param summaryStatistics    a Collection of summaryStatistic objects
     */
    public void setSummaryStatistics(
        Collection<SummaryStatistic> summaryStatistics) {
        this.summaryStatistics = summaryStatistics;
    }

    /**
     * A Collection of category data of each variable in the 
     * requested data file.   Used in the recoding-GUI rendering proces
     */
    private Collection<VariableCategory> categories;

    /**
     * Getter for property categories
     * 
     * @return    a Collection of VariableCategory objects
     */
    public Collection<VariableCategory> getCategories() {
        return this.categories;
    }

    /**
     * Setter for property categories
     * 
     * @param categories    A Collection of VariableCategory objects
     */
    public void setCategories(Collection<VariableCategory> categories) {
        this.categories = categories;
    }

    // -----------------------------------------------------------------------
    // data for the Variable Table
    // -----------------------------------------------------------------------
    
    /**
     * ArrayList object that stores major metadata of all variables in 
     * the requested data file and backs the value attribute of 
     * h:dataTable (id = dataTable1) in the variable table of the jsp page.
     */
    private List<Object> dt4Display = new ArrayList<Object>();
    
    /**
     * Getter for property dt4Display
     *
     * @return    a List object of major metadata of all varibles
     */
    public List<Object> getDt4Display() {
        return (dt4Display);
    }

    /**
     * Setter for property dt4Display
     *
     * @param dt    a List object of major metadata of all variables
     */
    public void setDt4Display(List<Object> dt) {
        dt4Display = dt;
    }
    
    /**
     * Adds major metadata of all variables in the requested data file into
     * dt4Display.  The six cells of each row are as follows:
     * 
     * boolean: checkbox state,
     * String: variable type,
     * Long:   variable Id,
     * String: variable name,
     * String: variable label,
     * String: blank cell for summary statistics
     * 
     * @see       #dt4Display
     */
    private void initDt4Display() {
        int counter = 0;
        int dbglns = 25;
        for (Iterator el = dataVariables.iterator(); el.hasNext();) {
            DataVariable dv = (DataVariable) el.next();
            counter++;
            if (counter <= dbglns) {
                dbgLog.fine("dtId=" + dtId
                    + " : within initDt4Display: row no=" + counter);
            }

            List<Object> rw = new ArrayList<Object>();
            // 0-th: boolean (checked/unchecked)
            rw.add(new Boolean(false));
            
            // 1st: variable type
            if (dv.getVariableFormatType().getName().equals("numeric")) {
                if (dv.getVariableIntervalType() == null) {
                    rw.add("Continuous");
                } else {
                    if (dv.getVariableIntervalType().getId().intValue() == 2) {
                        rw.add("Continuous");
                    } else {
                        rw.add("Discrete");
                    }
                }
            } else {
                rw.add("Character");
            }

            // 2nd: ID
            rw.add(dv.getId().toString());
            
            // 3rd: Variable Name
            rw.add(dv.getName());
            
            // 4th: Variable Label
            if (dv.getLabel() != null) {
                rw.add(dv.getLabel());
            } else {
                rw.add("[label missing]");
            }
            
            // 5th: summary statistics(blank)
            // the content is generated by an AJAX call upon request
            rw.add("");
            /*
            // 6th: fileorder number
            rw.add(dv.getFileOrder());
             */ 
            // add a row
            dt4Display.add(rw);
        }
    }


    // <----------------------------------------------------------------------
    // </editor-fold>


    // -----------------------------------------------------------------------
    // Constructor and Init method
    // -----------------------------------------------------------------------
    // <editor-fold desc="Constructor and Init method">
    // defaultstate="collapsed"


    /**
     * The default constructor
     *
     */
    public AnalysisPage() {

    }

    /**
     * Returns a reference to the application-scoped bean
     * 
     * @return a bean that stores zelig-model-related information
     */
    protected AnalysisApplicationBean getAnalysisApplicationBean() {
        return (AnalysisApplicationBean) getBean("AnalysisApplicationBean");
    }
    /**
     * Prepares the state-keeping html components 
     * and their backing java objects for rendering SubsettingPage.jsp
     * 
     */
    public void init() {
            dbgLog.fine("\n***** init():start *****");
        super.init();
        try {
            // sets default values to html components
            _init();
            
                dbgLog.fine("pass _init() in init()");
            // gets the FacesContext instance
            FacesContext cntxt = FacesContext.getCurrentInstance();
            
            // gets the ExternalContext
            ExternalContext exCntxt = FacesContext.getCurrentInstance()
                .getExternalContext();
            
            // gets session data from the ExternalContext
            Map<String, Object> sessionMap = exCntxt.getSessionMap();
            

                dbgLog.finer("\ncontents of RequestParameterMap:\n"
                    + exCntxt.getRequestParameterMap());
                dbgLog.finer("\ncontents of SessionMap:\n"+sessionMap);

            
            // gets the request header data
            Map<String, String> rqustHdrMp = exCntxt.getRequestHeaderMap();
            
                dbgLog.fine("\nRequest Header Values Map:\n" + rqustHdrMp);
                dbgLog.fine("\nRequest Header Values Map(user-agent):"
                    + rqustHdrMp.get("user-agent"));
            
            // gets an end-user's browser type
            if (isBrowserFirefox(rqustHdrMp.get("user-agent"))) {
                dbgLog.fine("user's browser is firefox");
                browserType = "Firefox";
            } else {
                browserType = "notFirefox";
            }

            // gets the current view state value (for post-back-checking)
            String currentViewStateValue = exCntxt.getRequestParameterMap()
                .get(ResponseStateManager.VIEW_STATE_PARAM);
            
                dbgLog.fine("ViewState value=" + currentViewStateValue);
                dbgLog.fine("VDCRequestBean: current VDC URL ="
                + getVDCRequestBean().getCurrentVDCURL());
            
            // Stores the URL of the requested study 
            setStudyURL(getVDCRequestBean().getCurrentVDCURL());
            
                dbgLog.fine("VDCRequestBean: studyId ="
                    + getVDCRequestBean().getStudyId());
                dbgLog.fine("VDCRequestBean =" + getVDCRequestBean());

             dbgLog.fine("selected tab(init())="+getTabSet1().getSelected());

            // set tab if it was it was sent as pamameter or part of request bean
            if (getTab() != null) {
                getTabSet1().setSelected(getTab());
                dbgLog.fine("getTab()= "+getTab());
            } else if (getVDCRequestBean().getSelectedTab() != null) {
                dbgLog.fine("tab from the requestBean="+getVDCRequestBean().getSelectedTab());
            
                getTabSet1().setSelected(getVDCRequestBean().getSelectedTab());
            }
            
            
            
            /*
            // Deletes session-scoped objects if this page is rendered 
            // for the first time, i.e. not post-back
            */
            if (currentViewStateValue == null || 
                getVDCRequestBean().getDtId() != null) {
                // The first time visit to the SubsettingPage.jsp

                List<String> sessionObjects = new ArrayList<String>();
                
                Collections.addAll(sessionObjects, "dt4Display", "varCart",
                    "varSetAdvStat", "groupPanel8belowRendered", 
                    "advStatVarRBox1", "advStatVarRBox2", "advStatVarRBox3",
                    "setxDiffVarBox1", "setxDiffVarBox2",
                     "checkboxSelectUnselectAllSelected",
                    "checkboxSelectUnselectAllRendered", "currentModelName", 
                    "currentRecodeVariableId","currentRecodeVariableName", 
                    "recodedVarSet","recodeSchema", "baseVarToDerivedVar",
                    "derivedVarToBaseVar", "recodeVarNameSet",
                    "selectedNoRows", "msgEdaButtonTxt", "msgDwnldButtonTxt",
                     "msgAdvStatButtonTxt","gridPanelModelInfoBox",
                     "lastSimCndtnSelected", "resultInfo",
                     "wrapSubsettingInstructionRendered", 
                     "wrapNonSubsettingInstructionRendered", "modelHelpLinkURL"
                     );

                for (String obj : sessionObjects) {
                    if (sessionMap.containsKey(obj)) {
                        cntxt.getExternalContext().getSessionMap().remove(obj);
                    }
                }
                
                    dbgLog.fine("left-over objects of the previous session"
                        +" have been removed");
            }

            // Gets the datatable Id if we're coming from editVariabePage
            if (dtId == null) {
                dtId = getVDCRequestBean().getDtId();
                    dbgLog.fine("dtId(null case: came from editVariablePage)="
                        +dtId);
            }

            // we need to create the VariableServiceBean
            if (dtId != null) {
                    dbgLog.fine("Init() enters non-null-dtId case: dtId="
                    + dtId);
                // Gets the requested data table by its Id
                dataTable = variableService.getDataTable(dtId);
                
                // Exposes the data file name to SubsettingPage.jsp
                setFileName(dataTable.getStudyFile().getFileName());
                
                    dbgLog.fine("file Name=" + fileName);
                

                // Retrieves each var's data from the data table
                // and saves them in Collection<DataVariable> dataVariables
                dataVariables.addAll(dataTable.getDataVariables());
                
                    dbgLog.fine("pass the addAll line");
                
                // Gets VDCUser-related data
                VDCUser user = null;
                if (getVDCSessionBean().getLoginBean() != null) {
                    user = getVDCSessionBean().getLoginBean().getUser();
                }
                
                VDC vdc = getVDCRequestBean().getCurrentVDC();
                    dbgLog.fine("VDCUser instnce=" + user);
                    dbgLog.fine("VDC instnce=" + vdc);
                
                //  Gets the StudyFile object via the dataTable object.
                //  This StudyFile determines whether
                //  Subsetting functionalities are rendered or not
                //  in SubsettingPage.jsp
                    dbgLog.fine("checking this end-user's permission status");
                StudyFile sf = dataTable.getStudyFile();
                HttpServletRequest request = 
                    (HttpServletRequest)this.getExternalContext().getRequest();

                if (sf.isSubsetRestrictedForUser(user, vdc, 
                    getVDCSessionBean().getIpUserGroup())) {
                        dbgLog.fine("restricted=yes: this user "+
                            "does not have the subsetting permission");
                    subsettingPageAccess = Boolean.FALSE;
                    txtNonSubsettingInstruction.setRendered(true);
                    wrapNonSubsettingInstructionRendered=Boolean.TRUE;
                    wrapSubsettingInstructionRendered=Boolean.FALSE;
                    hideSubsettingFunctions();
                } else {
                    // Sets the state of the Subsetting Page to subsettable
                    subsettingPageAccess = Boolean.TRUE;
                    // sets the default rendering state of the checkbox 
                    // checkboxSelectUnselectAll shown (TRUE)
                    checkboxSelectUnselectAllRendered=Boolean.TRUE;
                    checkboxSelectUnselectAllSelected=Boolean.FALSE;
                    
                    wrapNonSubsettingInstructionRendered=Boolean.FALSE;
                    wrapSubsettingInstructionRendered=Boolean.TRUE;
                        dbgLog.fine("restricted=no: " +
                            "this user has the subsetting permission");
                }
                
                    dbgLog.fine("Number of vars in this data table(" +
                    dtId + ")=" + dataTable.getVarQuantity());
                
                // Sets data for the variable table in the subsetting page.
                // Variable data are stored in List<Object> dt4Display
                if (!sessionMap.containsKey("dt4Display")) {
                    // dt4Display does not exist => 
                    // the page was rendered for the first time
                    // not a post-back case
                        dbgLog.fine("This is the 1st-time visit to this page:"+
                            "(dt4Display does not exist in the session map)");
                    // Fills List<Object> dt4Display with data
                    // and newly creaded data 
                    initDt4Display();
                    
                        dbgLog.fine("how many variables in dt4Display="
                            + getDt4Display().size());
                    
                    // Adds state-keeping objects to the session Map
                    // 
                    // List<Object>: The data for the variable table
                    sessionMap.put("dt4Display", dt4Display);

                    // Map<String, String>: The currently selected variables
                    sessionMap.put("varCart", varCart);

                    // Collection<Option> varSetAdvStat: user-selected
                    // variables behind the LHS variable list box
                    sessionMap.put("varSetAdvStat", varSetAdvStat);
                        dbgLog.fine("varSetAdvStat:\n"+varSetAdvStat);
                    // ui:PanelGroup component that shows/hides the pane for
                    // the advanced statistics. 
                    // The rendered attribute of this PanelGroup object
                    // must be state-kept
                    // deprecated: serialization-unsafe approach
                    // sessionMap.put("groupPanel8below", groupPanel8below);
                    // Hides the pane
                    groupPanel8belowRendered=Boolean.FALSE;
                    sessionMap.put("groupPanel8belowRendered", 
                        groupPanel8belowRendered);
                    
                    // h:panelGrid component that contains the model help
                    // information box in the advanced statistics pane.
                    // The rendered attribute of this HtmlPanelGrid object
                    // must be state-kept
                    // deprecated: 
                    // sessionMap.put("gridPanelModelInfoBox",
                    //    gridPanelModelInfoBox);
                    
                    /**/
                    // Hides the pane
                    gridPanelModelInfoBoxRendered = Boolean.FALSE; 
                    sessionMap.put("gridPanelModelInfoBoxRendered",
                        gridPanelModelInfoBoxRendered);
                        
                        // to do
                        // accessors: get/set
                        // JSP rendered="#{AnalysisPage.gridPanelModelInfoBoxRendered}"
                    
                    // Collection<Option> advStatVarRBox1
                    sessionMap.put("advStatVarRBox1", advStatVarRBox1);
                    sessionMap.put("advStatVarRBox2", advStatVarRBox2);
                    sessionMap.put("advStatVarRBox3", advStatVarRBox3);
                    
                    sessionMap.put("lastSimCndtnSelected", "0");

                    // ui:checkbox
                    // Checkbox object
                    // method       tag attribute
                    // setSelected   selectedValue
                    // setRendered   rendered
                    //
                    // deprecated: use of component-binding
                    //sessionMap.put("checkboxSelectUnselectAll",
                    //    checkboxSelectUnselectAll);
                    // 
                    // Saves the selected attribute
                    
                    sessionMap.put("checkboxSelectUnselectAllSelected",
                        checkboxSelectUnselectAllSelected);
                        
                    // Saves the rendered attribute
                    
                    sessionMap.put("checkboxSelectUnselectAllRendered",
                        checkboxSelectUnselectAllRendered);

                    sessionMap.put("wrapSubsettingInstructionRendered",
                        wrapSubsettingInstructionRendered);
                    sessionMap.put("wrapNonSubsettingInstructionRendered",
                        wrapNonSubsettingInstructionRendered);                    
                    
                    // String selectedNoRows
                    selectedNoRows = Integer.toString(INITIAL_ROW_NO);
                    howManyRowsOptions.setSelectedValue(selectedNoRows);
                    sessionMap.put("selectedNoRows", selectedNoRows);
                    
                    
                        dbgLog.fine("selectedNoRows=" + selectedNoRows);
                        dbgLog.fine("1st time visit: "+
                            "selected value for howManyRows="
                            + howManyRowsOptions.getSelectedValue());
                } else {
                    // Postback cases (not 1st-time visit to this page)
                    // Applies the stored data to the key page-scoped objects
                    
                    
                        dbgLog.fine("Postback(non-1st-time) case: "+
                            "dt4Display was found in the session map=>");
                    
                    // Gets the stored object backing the value attribute 
                    // of h:dataTable for the variable table 
                    setDt4Display((List<Object>) cntxt.getExternalContext()
                        .getSessionMap().get("dt4Display"));

                    // Gets the stored object that records 
                    // the currently selected variables 
                    varCart = (Map<String, String>) sessionMap.get("varCart");
                        dbgLog.fine("varCart:\n"+varCart);

                    // Gets the stored object backing the items attribute of 
                    // ui:listbox for the LHS list-box component, which is
                    // located in each tab
                    varSetAdvStat = (Collection<Option>) sessionMap
                        .get("varSetAdvStat");
                        dbgLog.fine("varSetAdvStat:\n"+varSetAdvStat);

                    // Gets the stored object backing the items attribute of
                    // ui:listbox for the 1st RHS variable box
                    // located in the advanced statistics tab
                    advStatVarRBox1 = (Collection<Option>) sessionMap
                        .get("advStatVarRBox1");
                    
                    // ditto (2nd)
                    advStatVarRBox2 = (Collection<Option>) sessionMap
                        .get("advStatVarRBox2");
                    
                    // ditto (3rd)
                    advStatVarRBox3 = (Collection<Option>) sessionMap
                        .get("advStatVarRBox3");
                        dbgLog.fine("advStatVarRBox1:\n"+advStatVarRBox1);
                        dbgLog.fine("advStatVarRBox2:\n"+advStatVarRBox2);
                        dbgLog.fine("advStatVarRBox3:\n"+advStatVarRBox3);
                    
                    radioButtonGroup1DefaultOptions.setSelectedValue( (String)sessionMap.get("lastSimCndtnSelected"));
                        dbgLog.fine("lastSimCndtnSelected= "+ sessionMap.get("lastSimCndtnSelected"));

                    // Gets the stored object backing the items attribute
                    // of ui:dropDown for the 1st setx variable selector
                    setxDiffVarBox1 = (Collection<Option>) sessionMap
                        .get("setxDiffVarBox1");
                        
                    // ditto (2nd)
                    setxDiffVarBox2 = (Collection<Option>) sessionMap
                        .get("setxDiffVarBox2");
                        
                    // String object backing the currently selected model name
                    currentModelName = (String) sessionMap
                        .get("currentModelName");
                    
                        dbgLog.fine("\nSelected model name(after post-back)="
                            + currentModelName+"\n");
                    // deprecated: 
                    // checkbox component
                    // Gets the stored object backing the binding attribute
                    // of ui:checkbox for the select-unselect check box
                    // checkboxSelectUnselectAll = (Checkbox) sessionMap
                    //    .get("checkboxSelectUnselectAll");
                    
                    // new approach
                    // Updates the following two attributes at least
                    // class method  tag attr
                    // setSelected   selectedValue
                    // setRendered   rendered

                    // Gets the selected attribute
                    checkboxSelectUnselectAllSelected = (Boolean) sessionMap
                        .get("checkboxSelectUnselectAllSelected");
                    checkboxSelectUnselectAll.setSelected(
                        checkboxSelectUnselectAllSelected);
                    
                    // Gets the rendered attribute

                    checkboxSelectUnselectAllRendered = (Boolean) sessionMap
                        .get("checkboxSelectUnselectAllRendered");
                    checkboxSelectUnselectAll.setRendered(
                        checkboxSelectUnselectAllRendered);

                    wrapSubsettingInstructionRendered = (Boolean) sessionMap
                        .get("wrapSubsettingInstructionRendered");
                    wrapNonSubsettingInstructionRendered = (Boolean) sessionMap
                        .get("wrapNonSubsettingInstructionRendered");                        
                    
                    // Gets the stored object backing the selected attribute
                    // of ui:dropDown for the menu of choosing
                    // an option of how many row per table
                    selectedNoRows = (String)sessionMap.get("selectedNoRows");
                        dbgLog.fine("post-back case: "+
                            "returned the selected value for howManyRows="
                            + selectedNoRows);
                    
                    howManyRowsOptions.setSelectedValue(selectedNoRows);
                    
                        dbgLog.fine("post-back case:"+
                            " selected value for howManyRows="
                            + howManyRowsOptions.getSelectedValue());
                            
                        if (currentRecodeVariableId == null) {
                            dbgLog.fine("currentRecodeVariableId is null");
                        } else {
                            dbgLog.fine("currentRecodeVariableId="
                                + currentRecodeVariableId);
                        }
                        dbgLog.fine("currentRecodeVariableId: "+
                            "received value from sessionMap="
                             + (String) sessionMap
                             .get("currentRecodeVariableId"));
                    
                    currentRecodeVariableId = (String) sessionMap
                        .get("currentRecodeVariableId");
                        
                        dbgLog.fine("new currentRecodeVariableId="
                            + currentRecodeVariableId);
                    
                    // Gets the stored object backing the rendered attribute
                    // of ui:PaneGroup for groupPanel8below
                    groupPanel8belowRendered =(Boolean) 
                        sessionMap.get("groupPanel8belowRendered");
                    dbgLog.fine("groupPanel8belowRendered(map)="+
                        sessionMap.get("groupPanel8belowRendered"));
                    dbgLog.fine("groupPanel8belowRendered(result)="+
                        groupPanel8belowRendered);
                    // deprecated approach
                    // Gets the stored object backing the binding attribute of
                    // ui: PanelGroup for groupPanel8below
                    // cntxt.getExternalContext().getSessionMap()
                    // .get("groupPanel8below"));

                    
                    // Gets the stored object backing the value attribute of
                    // h:outputText (id=recodeHdrVariable) for the currently 
                    // selected recode variable name that is used in the 
                    // column header of the recode variable editing table
                    if (!sessionMap.containsKey("currentRecodeVariableName")) {
                        sessionMap.put("currentRecodeVariableName",
                            currentRecodeVariableName);
                    } else {
                        currentRecodeVariableName = (String) sessionMap
                            .get("currentRecodeVariableName");
                    }
                    
                        dbgLog.fine("new currentRecodeVariableName="
                            + currentRecodeVariableName);
                    // deprecated (component-binding is used now)
                    // Gets the stored object backing the value attribute
                    // of h:inputText for the label of the recode variable
                    // (id= recodeTargetVarLabel)
                    /*
                    if (!sessionMap.containsKey("recodeVariableLabel")) {
                        sessionMap.put("recodeVariableLabel",
                            recodeVariableLabel);
                    } else {
                        recodeVariableLabel = (String) sessionMap
                            .get("recodeVariableLabel");
                    }
                    */
                    
                    // Gets the stored object backing the value attribute of
                    // h:dataTable for the recoding table 
                    // whose id is "recodeTable"
                    
                    if (!sessionMap.containsKey("recodeDataList")) {
                        sessionMap.put("recodeDataList", recodeDataList);
                    } else {
                        recodeDataList = (List<Object>) sessionMap
                            .get("recodeDataList");
                    }
                    
                    // Gets the stored object for recodeSchema that 
                    // stores each recode variable's recodeDataList
                    // as a hash table. Not exposed to SubsettingPage.jsp
                    if (!sessionMap.containsKey("recodeSchema")) {
                        sessionMap.put("recodeSchema", recodeSchema);
                    } else {
                        recodeSchema = (Map<String, List<Object>>) sessionMap
                            .get("recodeSchema");
                    }

                    // Gets the stored object for the value attribute
                    // of h:dataTable for the table of recode variables
                    // whose id is "recodedVarTable"
                    if (!sessionMap.containsKey("recodedVarSet")) {
                        sessionMap.put("recodedVarSet",recodedVarSet);
                    } else {
                        recodedVarSet = (List<Object>) sessionMap
                            .get("recodedVarSet");
                    }
                    
                    // Gets the stored object for derivedVarToBaseVar
                    // that maps a new variable to its base one.
                    // No exposed to SubsettingPage.jsp
                    if (!sessionMap.containsKey("derivedVarToBaseVar")) {
                        sessionMap.put("derivedVarToBaseVar",
                            derivedVarToBaseVar);
                    } else {
                        derivedVarToBaseVar = (Map<String, String>) sessionMap
                            .get("derivedVarToBaseVar");
                    }

                    // Gets the stored object for baseVarToDerivedVar
                    // that maps a base variable to its derived variables.
                    // Not exposed to SubsettingPage.jsp
                    if (!sessionMap.containsKey("baseVarToDerivedVar")) {
                        sessionMap.put("baseVarToDerivedVar",
                            baseVarToDerivedVar);
                    } else {
                        baseVarToDerivedVar = (Map<String, Set<String>>) 
                            sessionMap.get("baseVarToDerivedVar");
                    }

                    // Gets the stored object for recodeVarNameSet that
                    // checks the uniqueness of a name 
                    // for a new recode-variable.
                    // Not exposed to SubsettingPage.jsp
                    if (!sessionMap.containsKey("recodeVarNameSet")) {
                        sessionMap.put("recodeVarNameSet", recodeVarNameSet);
                    } else {
                        recodeVarNameSet = (Set<String>) sessionMap
                            .get("recodeVarNameSet");
                    }
                    
                    // Resets the properties (rendered and text) of
                    // msgSaveRecodeBttn (ui:staticText) that shows
                    // error messages for the action of SaveRecodeBttn
                    resetMsgSaveRecodeBttn();
                    
                    // Rests the properties (rendered and text) of
                    // msgVariableSelection (ui:staticText) that shows
                    // error message when the base varaible for
                    // a recoded variable is un-selected in the variable table
                    resetMsgVariableSelection();

                    // Gets the stored object backing msgDwnldButtonTxt that
                    // shows error messages for the action of dwnldButton
                    if (!sessionMap.containsKey("msgDwnldButtonTxt")) {
                        sessionMap.put("msgDwnldButtonTxt", msgDwnldButtonTxt);
                    } else {
                        msgDwnldButtonTxt = (String) sessionMap
                            .get("msgDwnldButtonTxt");
                    }
                    // Hides the error message text for dwnldButton
                    msgDwnldButton.setVisible(false);
                    
                    // Gets the stored object backing msgEdaButtonTxt that
                    // shows error messages for the action of edaButton
                    if (!sessionMap.containsKey("msgEdaButtonTxt")) {
                        sessionMap.put("msgEdaButtonTxt", msgEdaButtonTxt);
                    } else {
                        msgEdaButtonTxt = (String) sessionMap
                            .get("msgEdaButtonTxt");
                    }
                    // Hides the error message text for edaButton
                    msgEdaButton.setVisible(false);

                    // Gets the stored object backing msgAdvStatButtonTxt that
                    // shows error messages for the action of edaButton
                    if (!sessionMap.containsKey("msgAdvStatButtonTxt")) {
                        sessionMap.put("msgAdvStatButtonTxt", msgAdvStatButtonTxt);
                    } else {
                        msgAdvStatButtonTxt = (String) sessionMap
                            .get("msgAdvStatButtonTxt");
                    }
                    // Hides the error message text for edaButton
                    msgAdvStatButton.setVisible(false);


//                    if (sessionMap.containsKey("modelHelpLinkURL")) {
//                        dbgLog.fine("modelHelpLinkURL="+sessionMap.get("modelHelpLinkURL"));
//                        modelHelpLinkURL= (String) sessionMap.get("modelHelpLinkURL");
//                    }


                    // end of post-back cases
                }

            } else {
                // dtId is not available case
                dbgLog.severe("ERROR: AanalysisPage.java: "+
                    "without a serviceBean or a dtId");
            }

            // Stores the title, ID, and citation data of the requested study
            if (sessionMap.containsKey(getStudyUIclassName())) {

                StudyUI sui = (StudyUI) sessionMap.get(getStudyUIclassName());

                // Stores the title, Id, and Citation of the requested study
                setStudyTitle(sui.getStudy().getTitle());
                setStudyId(sui.getStudy().getId());
                setCitation(sui.getStudy().getCitation(false));
                
                    dbgLog.fine("StudyUIclassName was found"+
                        " in the session Map");
                    dbgLog.fine("Study Title="+studyTitle);
                    dbgLog.fine("Study Id="+studyId);
                    dbgLog.fine("Ciation="+citation);
            } else {
                    dbgLog.fine("StudyUIclassName was not in the session Map");
            }
            
                dbgLog.finer("\nSpec Map:\n"+
                    getAnalysisApplicationBean().getSpecMap());
                dbgLog.finer("\nMenu Option List:\n"+
                    getAnalysisApplicationBean().getModelMenuOptions());
                dbgLog.finer("\ncontents of the cart:\n" + varCart);

        } catch (Exception e) {
            dbgLog.severe("AnalysisPage Initialization Failure");
            e.printStackTrace();
            throw e instanceof FacesException ? (FacesException) e
                : new FacesException(e);
        } // end of try-catch block
        
            dbgLog.fine("init(): current tab id=" + tabSet1.getSelected());
            dbgLog.fine("***** init():end *****\n\n");
    }
    
    // end of doInit() -------------------------------------------------------


    public void preprocess() {
    }

    public void prerender() {
    }

    public void destroy() {
    }

    // </editor-fold>

    // -----------------------------------------------------------------------
    // Access control methods
    // -----------------------------------------------------------------------

    public String gotoEditVariableAction() {
        String dvFilter = "";
        for (Option item : getVarSetAdvStat()) {
            dvFilter += (String) item.getValue() + ",";
        }

        getVDCRequestBean().setDtId(dtId);
        getVDCRequestBean().setDvFilter(dvFilter);

        return "editVariable";
    }

    public boolean isEditVariableActionRendered() {
        boolean render = false;

        if (getVDCSessionBean().getLoginBean() != null) {
            Study study = dataTable.getStudyFile().getFileCategory().getStudy();
            boolean authorized = study
                .isUserAuthorizedToEdit(getVDCSessionBean().getLoginBean()
                    .getUser());
            boolean locked = study.getStudyLock() != null;

            render = authorized && !locked;
        }

        return render;
    }
    



    // hide subsetting functions according to the user's status
    /**
     * Hides the subsetting related components when an end-user does not 
     * have permission to subset a data file
     *
     */
    public void hideSubsettingFunctions() {
        // Sets the rendered attribute of the following components as follows:
        
        // 1. Hides tabSet above the variable table
        tabSet1.setRendered(false);
        
        // 2. Hides the subsetting intruction text
        //txtSubsettingInstruction.setRendered(false);
        
        // 3. Shows the non-Subsetting intruction text
        //txtNonSubsettingInstruction.setRendered(true);

        // 4. Hides the select-all checkbox in the header(1st column) of 
        //    the variable table
        checkboxSelectUnselectAll.setRendered(false);
        // Stores this hide state in a Boolean object
        checkboxSelectUnselectAllRendered=Boolean.FALSE;
        
        // 5. Hides the variable-checkboxes in the 1st column of 
        //    the variable table
        varCheckboxx.setRendered(false);
        
        // 6. Hides the title of the recoded-var table
        recodedVarTableTitle.setRendered(false);

        // 7. Hides the panel grid that contains the recoded-variable table
        //pgRecodedVarTable.setRendered(false);
        groupPanelRecodeTableArea.setRendered(false);
    }

    // <----------------------------------------------------------------------
    //  service-request-related methods
    // <----------------------------------------------------------------------
    // <editor-fold desc="service-request-related methods">

    
    /**
     * Returns a List object that stores major metadata for all variables 
     * selected by an end-user
     *
     * @return    List of DataVariable objects that stores metadata
     */
    public List<DataVariable> getDataVariableForRequest() {
        List<DataVariable> dvs = new ArrayList<DataVariable>();
        for (Iterator el = dataVariables.iterator(); el.hasNext();) {
            DataVariable dv = (DataVariable) el.next();
            String keyS = dv.getId().toString();
            if (varCart.containsKey(keyS)) {
                dvs.add(dv);
            }
        }
        return dvs;
    }

    /**
     * Returns the name of a given variable whose id is known.
     * Because dt4Display is not a HashMap but a List, 
     * loop-through is necessary
     *
     * @param varId    the id of a given variable
     * @return    the name of a given variable
     */
    public String getVariableNamefromId(String varId) {

        for (int i = 0; i < dt4Display.size(); i++) {
            if (((String) ((ArrayList) dt4Display.get(i)).get(2)).equals(varId)) {
                return (String) ((ArrayList) dt4Display.get(i)).get(3);

            }
        }
        return null;
    }


    /**
     * Returns the label of a given variable whose id is known.
     * Because dt4Display is not a HashMap but a List, 
     * loop-through is necessary
     *
     * @param varId    the id of a given variable
     * @return    the label of a given variable
     */
    public String getVariableLabelfromId(String varId) {

        for (int i = 0; i < dt4Display.size(); i++) {
            if (((String) ((ArrayList) dt4Display.get(i)).get(2)).equals(varId)) {
                return (String) ((ArrayList) dt4Display.get(i)).get(4);
            }
        }
        return null;
    }

    /**
     * Returns the file-order of a given variable whose id is known.
     * Because dt4Display is not a HashMap but a List, 
     * loop-through is necessary
     *
     * @param varId    the id of a given variable
     * @return    the file-order number of a given variable
     */
    /*
    public String getVariableFileOderfromId(String varId) {

        for (int i = 0; i < dt4Display.size(); i++) {
            if (((String) ((ArrayList) dt4Display.get(i)).get(2)).equals(varId)) {
                return (String) ((ArrayList) dt4Display.get(i)).get(6);

            }
        }
        return null;
    }
    */
    /**
     * Gets the row of metadata of a variable whose Id is given by a String
     * object
     *
     * @param varId    a given variable's ID as a String object
     * @return    a DataVariable instance that contains major metadata of the
     *            requested variable
     */
    public DataVariable getVariableById(String varId) {

        DataVariable dv = null;
        for (Iterator el = dataVariables.iterator(); el.hasNext();) {
            dv = (DataVariable) el.next();
            // Id is Long
            if (dv.getId().toString().equals(varId)) {
                return dv;
            }
        }
        return dv;
    }
    
    
    /**
     * 
     *
     * @param dvs   
     * @return      
     */
    public List<String> generateVariableIdList(List<String> dvs) {
        List<String> variableIdList = new ArrayList<String>();
        if (dvs != null) {
            for (String el : dvs){
                variableIdList.add("v" + el);
            }
        }
        return variableIdList;
    }

    /**
     * 
     *
     * @param     
     * @return    
     */
    
    public List<String> getVariableListForRequest(){
        List<String> variableList = new ArrayList();
        if (getDataVariableForRequest() != null) {
            Iterator iter = getDataVariableForRequest().iterator();
            while (iter.hasNext()) {
                DataVariable dv = (DataVariable) iter.next();
                variableList.add(dv.getId().toString());
            }
        }
        return variableList;
    }
    
    public List<String> getVariableIdListForRequest(){
        List<String> ids = generateVariableIdList(getVariableListForRequest());
        return ids;
    }

    /**
     * 
     *
     * @param     
     * @return    
     */
    
    public List<String> getVariableOrderForRequest(){
        List<String> variableOrder = new ArrayList();
        if (getDataVariableForRequest() != null) {
            Iterator iter = getDataVariableForRequest().iterator();
            while (iter.hasNext()) {
                DataVariable dv = (DataVariable) iter.next();
                // the susbsetting parameter starts from 1 not 0,
                // add 1 to the number
                variableOrder.add( Integer.toString(dv.getFileOrder()+1) );
            }
        }
        return variableOrder;
    }

    /**
     * 
     *
     * @return    A List of file-order numbers
     */
    public List<Integer> getFileOrderForRequest() {
        List<DataVariable> dvs = new ArrayList<DataVariable>();
        List<Integer> fileOrderForRequest = new ArrayList<Integer>();
        for (Iterator el = dataVariables.iterator(); el.hasNext();) {
            DataVariable dv = (DataVariable) el.next();
            String keyS = dv.getId().toString();
            if (varCart.containsKey(keyS)) {
                fileOrderForRequest.add(dv.getFileOrder());
            }
        }
        Collections.sort(fileOrderForRequest);
        return fileOrderForRequest;
    }

    /**
     * 
     *
     * @return    
     */
    public Map<Long, List<List<Integer>>> getSubsettingMetaData(){

        Map<Long, List<List<Integer>>> varMetaSet = new LinkedHashMap<Long, List<List<Integer>>>();

        List<DataVariable> dvs = getDataVariableForRequest();

        if (dvs != null) {
            for (int i = 0; i < dvs.size();i++  ){

                DataVariable dv = dvs.get(i);

                List<Integer> varMeta = new ArrayList<Integer>();

                varMeta.add( Integer.valueOf(dv.getFileStartPosition().toString()) );
                varMeta.add( Integer.valueOf(dv.getFileEndPosition().toString()) );
                // raw data: 1: numeric 2: character=> 0: numeric; 1 character
                varMeta.add( Integer.valueOf( (int)(dv.getVariableFormatType().getId()-1)  ) ); 
                
                if ( dv.getNumberOfDecimalPoints() == null ) {
                    varMeta.add ( 0 ); 
                } else if ( dv.getNumberOfDecimalPoints().toString().equals ("") ) {
                    varMeta.add ( 0 ); 
                } else {
                    varMeta.add( Integer.valueOf(dv.getNumberOfDecimalPoints().toString()) ); 
                }

                Long recordSegmentNumber = dv.getRecordSegmentNumber(); 

                if ( varMetaSet.get(recordSegmentNumber) == null ) {
                    List<List<Integer>> cardVarMetaSet = new LinkedList<List<Integer>>();
                    varMetaSet.put(recordSegmentNumber, cardVarMetaSet); 
                }

                varMetaSet.get(recordSegmentNumber).add(varMeta); 
            
            }
        }

        return varMetaSet; 
    }
        
    public Map<String, String> getValueTableForRequestedVariable(DataVariable dv){
        List<VariableCategory> varCat = new ArrayList<VariableCategory>();
        varCat.addAll(dv.getCategories());
        Map<String, String> vl = new HashMap<String, String>();
        for (VariableCategory vc : varCat){
            vl.put(vc.getValue(), vc.getLabel());
        }
        return vl;
    }
    
    
    public Map<String, Map<String, String>> getValueTableForRequestedVariables(List<DataVariable> dvs){
        Map<String, Map<String, String>> vls = new LinkedHashMap<String, Map<String, String>>();
        for (DataVariable dv : dvs){
            List<VariableCategory> varCat = new ArrayList<VariableCategory>();
            varCat.addAll(dv.getCategories());
            Map<String, String> vl = new HashMap<String, String>();
            for (VariableCategory vc : varCat){
                if (vc.getLabel() != null){
                    vl.put(vc.getValue(), vc.getLabel());
                }
            }
            if (vl.size() > 0){
                vls.put("v"+dv.getId(), vl);
            }
        }
        return vls;
    }

    public Map<String, Map<String, String>> getValueTablesOfRecodedVariables(){
        Map<String, Map<String, String>> vls = new LinkedHashMap<String, Map<String, String>>();
        List<String> varId = getRawRecodedVarIdSet();
        for (int i=0; i< varId.size();i++){
            Map<String, String> vl = new HashMap<String, String>();
            // get the outer-list by Id
            List<Object> rdtbl = (List<Object>) recodeSchema.get(varId.get(i));
            // loop through inner lists
            for (int j=0; j< rdtbl.size();j++){
                List<Object> rw = (List<Object>)rdtbl.get(j);
                if (rw.get(2) !=null){
                    vl.put((String)rw.get(1), (String)rw.get(2));
                }
            }
            if (vl.size() > 0){
                vls.put("v"+varId.get(i),vl);
            }
        }
        return vls;
    }
    
    public Map<String, Map<String, String>> getValueTablesForAllRequestedVariables(){
        Map<String, Map<String, String>> vls = getValueTableForRequestedVariables(getDataVariableForRequest());
        Map<String, Map<String, String>> vln = getValueTablesOfRecodedVariables();
        vls.putAll(vln);
        return vls;
    }
    
    public List<String> getRecodedVarIdSet() {
        List<String> vi = new ArrayList<String>();
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            vi.add("v" + (String) rvs.get(2));
        }
        return vi;
    }    
    
    public List<String> getRawRecodedVarIdSet() {
        List<String> vi = new ArrayList<String>();
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            vi.add((String) rvs.get(2));
        }
        return vi;
    }    
    
    public List<String> getRecodedVarNameSet() {
        List<String> vn = new ArrayList<String>();
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            vn.add((String) rvs.get(0));
        }
        return vn;
    }
    
    public List<String> getBaseVarIdSetFromRecodedVarIdSet() {
        List<String> vi = new ArrayList<String>();
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            vi.add("v" +  derivedVarToBaseVar.get( (String) rvs.get(2) ) );
        }
        return vi;
    }
    
    public List<String> getBaseVarNameSetFromRecodedVarIdSet() {
        List<String> vn = new ArrayList<String>();
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            vn.add( getVariableNamefromId( derivedVarToBaseVar.get( (String) rvs.get(2) ) ) );
        }
        return vn;
    }
    
    
    
    public List<String> getRecodedVarLabelSet() {
        List<String> vl = new ArrayList<String>();
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            vl.add((String) rvs.get(1));
        }
        return vl;
    }
    
    
    // get variable type (int) from a given row of the dataTable
    public List<String> getRecodedVariableType() {
        List<String> vt = new ArrayList<String>();
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            dbgLog.fine("rvs="+rvs);
            String newVarId = (String) rvs.get(2);
            int rawType = getVariableType(  getVariableById( derivedVarToBaseVar.get(newVarId) ) ) ;
            if (rawType == 2){
                rawType = 1;
            }
            vt.add( Integer.toString(rawType));
        }
        return vt;
    }
    
    
    // get variable type (int) from a given row of the dataTable
    public List<String> getBaseVariableTypeForRecodedVariable() {
        List<String> vt = new ArrayList<String>();
        for (int i = 0; i < recodedVarSet.size(); i++) {
            List<Object> rvs = (List<Object>) recodedVarSet.get(i);
            vt.add( Integer.toString(getVariableType( 
            getVariableById( derivedVarToBaseVar.get((String) rvs.get(2)) ) ) ) );
        }
        return vt;
    }

    
    
    public String getVariableHeaderForSubset() {
        String varHeader = null; 
        List<DataVariable> dvs = getDataVariableForRequest();
        List<String> vn = new ArrayList<String>();
        if (dvs != null) {
            for (Iterator el = dvs.iterator(); el.hasNext();) {
                DataVariable dv = (DataVariable) el.next();
                vn.add(dv.getName());
            }
            varHeader = StringUtils.join(vn, "\t");
            varHeader = varHeader + "\n";
        }
        return varHeader;
    }
    
    
    public Set<Integer> getFieldNumbersForSubsetting(){
        // create var ids for subsetting
        // data(int) are taken from DB's studyfile table -- FileOrder column
        Set<Integer> fields = new LinkedHashSet<Integer>();
        List<DataVariable> dvs = getDataVariableForRequest();

        if (dvs != null) {
            for (Iterator el = dvs.iterator(); el.hasNext();) {
                DataVariable dv = (DataVariable) el.next();
                fields.add(dv.getFileOrder());
            }
        }
        return fields;
    }
    
    
    
    
    // </editor-fold>
    
    // -----------------------------------------------------------------------
    // misc. methods 
    // -----------------------------------------------------------------------
    // <editor-fold desc="misc. methods">

    /**
     * Returns true if an end-user's brower is Firefox
     *
     * @param userAgent    hash value of the user-agent key in the request
     *                     header's map
     * @return    true if an end-user's brower is Firefox; false otherwise
     */
    public boolean isBrowserFirefox(String userAgent) {
        boolean rtvl = false;
        String regex = "Firefox";
        Pattern p = null;
        try {
            p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException pex) {
            pex.printStackTrace();
        }
        Matcher matcher = p.matcher(userAgent);
        rtvl = matcher.find();
        return rtvl;
    }

    /**
     *
     * @return    The url of the DSB
     */
    public String getDsbUrl(){

        String dsbUrl = System.getProperty("vdc.dsb.host");
        String dsbPort = System.getProperty("vdc.dsb.port");

        if (dsbPort != null) {
            dsbUrl += ":" + dsbPort;
        }

        if (dsbUrl == null) {
            dsbUrl = System.getProperty("vdc.dsb.url");
        }
        return dsbUrl;
    }

    // <----------------------------------------------------------------------
        public void writeRhistory(File frh, String rh){
            OutputStream outs = null;
            try {
                outs = new BufferedOutputStream(new FileOutputStream(frh));
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(outs, "utf8"), true);
                pw.println(rh);
               outs.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }



    public void zipFiles(OutputStream out, List<File> fllst) {
        ZipOutputStream zout = null;
        //BufferedInputStream infile = null;
        FileInputStream infile = null;
       
        zout = new ZipOutputStream(out);

        
        for (int i = 0; i< fllst.size(); i++) {
            try {
                infile = 
                    new FileInputStream(fllst.get(i));//new BufferedInputStream()
                
            } catch (FileNotFoundException e) {
                err.println("input file is not found");
                e.printStackTrace();
                try {
                    zout.close();
                } catch (ZipException ze) {
                    err.println("zip file invalid");
                    ze.printStackTrace();
                } catch (IOException ex) {
                    err.println("closing output file");
                    ex.printStackTrace();
                }
            }
            
            ZipEntry ze = new ZipEntry(fllst.get(i).getName());
            try {
                zout.putNextEntry(ze);
            /*
                int len;
                while ((len = infile.read())> 0) {
                    zout.write(len);
                }
            */

                byte[] dataBuffer = new byte[8192]; 

                int k = 0;
                while ( ( k = infile.read (dataBuffer) ) > 0 ) {
                    zout.write(dataBuffer,0,k);
                    //fileSize += i; 
                    out.flush(); 
                }
                
            } catch (ZipException zpe) {
                zpe.printStackTrace();
                err.println("zip file is invalid");
            } catch (IOException ie) {
                ie.printStackTrace();
                err.println("output file io-error");
            }
            
            try {
                infile.close();
            } catch (IOException ie) {
                err.println("error: closing input file");
            }
        }
        
        try {
            zout.close();
        } catch (ZipException zpe) {
            err.println("zip file invalid");
            zpe.printStackTrace();
        } catch (IOException ioe) {
            err.println("error closing zip file");
            ioe.printStackTrace();
        }

    }

    // </editor-fold>

    // end of this class
}
