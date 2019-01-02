package ocr;

import dk.picit.ai.Result;
import dk.picit.ai.Sample;
import dk.picit.ai.Target;
import dk.picit.ai.test.AIAsciiSample;
import dk.picit.ai.test.AIMainOCR;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * For testing a full ID read with the AI
 */
@SuppressWarnings("Duplicates")
public class OCRReaderID {

    public static final String[] shippingID = {
            "ABL",
            "AMF",
            "BKX",
            "BMO",
            "BOI",
            "BXR",
            "CAI",
            "CAU",
            "CCC",
            "CCL",
            "CDD",
            "CDK",
            "CLH",
            "CMA",
            "CNS",
            "COI",
            "CPI",
            "CPL",
            "CPW",
            "CRX",
            "CTX",
            "CXI",
            "DCS",
            "DNC",
            "ECM",
            "FAM",
            "FBX",
            "FCB",
            "FCB",
            "FCI",
            "FIX",
            "FSC",
            "FXL",
            "GAT",
            "GBC",
            "GES",
            "GLD",
            "GST",
            "HBT",
            "HDM",
            "HHA",
            "HJC",
            "HJL",
            "HJU",
            "HLX",
            "HNK",
            "ICO",
            "INB",
            "KKF",
            "MAE",
            "MAX",
            "MOF",
            "MOT",
            "MSC",
            "MSO",
            "MSU",
            "MTB",
            "MWC",
            "NEV",
            "NEW",
            "OCV",
            "OMF",
            "PCI",
            "PON",
            "RAL",
            "RAV",
            "SEG",
            "SHO",
            "TCI",
            "TCK",
            "TCL",
            "TCN",
            "TDR",
            "TDT",
            "TEM",
            "TES",
            "TGH",
            "TGT",
            "TIT",
            "TRI",
            "TRL",
            "TRX",
            "TTN",
            "UAC",
            "WLN",
            "WSC",
            "XIN"
    };

    private static final String[] FULL_IDS_OLD = {
            "dcsu385213822g1",
            "dcsu385214322g1",
            "cpwu208907122g1",
            "msuu281291922g1",
            "msuu500961842g1", //bad one
            "msuu281288422g1",
            "fcbu857089522g1",
            "tciu842100022g2",
            "cauu130094822g1",
            "newu200851022g1",
            "fcbu520536942g1",
            "hnku513379545g1",
            "hlxu507159142g1",
            "cauu014203022g1",
            "cauu11410322g1",
            "crxu4907493gb4310",
            "crxu9401379gb4510",
            "crxu4412853gb4310",
            "kkfu728844845g1",
            "ravu550416022rb"

    };

    private static List<String> TEST_IDS = new ArrayList<>();
    private static final String[] ALTER_IDS = {
            "DCSU 385213-8",
            "DCSU 385214-3",
            "CPWU 208907-1",
            "MSUU 281291-9",
            "MSUU 500961-8",
            "MSUU 281288-4",
            "FCBU 857089-5",
            "TCIU 842100-0",
            "CAUU 130094-8",
            "NEWU 200851-0",
            "FCBU 520536-9",
            "HNKU 513379-5",
            "HLXU 507159-1",
            "CAUU 014203-0",
            "CRXU 490749-3",
            "CRXU 940137-9",
            "CRXU 441285-3",
            "KKFU 728844-8",
            "RAVU 550416-0",
            //==========
            "BOIU 391046-8",
            "CAUU 014203-0",
            "CAUU 114103-9",
            "CAUU 114127-6",
            "CAUU 1300830",
            "CLHU 443851-1",
            "CPWU 215756-1",
            "CRXU 490749-3",
            "CRXU 940137-9",
            "CSTU 339004-2",
            "CTXU 347232-1",
            "CXIU 380144-5",
            "DCSU 370175-9",
            "DCSU 385213-8",
            "FCBU 857022-2",
            "FCBU 857303-0",
            "FCBU 857329-8",
            "FCBU 857855-6",
            "FCBU 951774-5",
            "FCBU 951776-6",
            "FCBU 951793-5",
            "FCBU 951796-1",
            "FXLU 905902-7",
            "GETU 590235-1",
            "HDMU 636268-9",
            "KKFU 728844-8",
            "MAXU 241728-1",
            "MSUU 283019-4",
            "MSUU 886009-3",
            "TCIU 630809-9",
            "TCIU 645669-2",
            "TCIU 500026-5",
            "TGHU 453213-8",
            "TGHU 465441-3",
            "TGHU 482121-2",
            "TITU 825142-2",
            "TITU 825666-1",
            "TITU 825666-1",
            "TITU 825666-1",
            "TITU 826142-0",
            "TITU 826142-0",
            //=============
            "FCBU 857017-5",
            "FCBU 857085-3",
            "FCBU 857034-4",
            "FCBU 857032-3",
            "FCBU 857087-4",
            "FCBU 857083-2",
            "FCBU 857082-7",
            "CPWU 215756-1",
            "CAUU 120046-6",
            "CAUU 114121-3",
            "CAUU 120080-4",
            "CAUU 130093-2",
            "CAUU 130107-6",
            "CAUU 130105-5",
            "CAUU 114120-8",
            "CAUU 120055-3",
            "CAUU 120058-0",
            "CAUU 130096-9",
            "CAUU 130106-0",
            "CAUU 014204-6",
            "CAUU 114128-1",
            "CAUU 014228-3",
            "CAUU 130091-1",
            "CAUU 114125-5",
            "CAUU 014210-7",
            "CAUU 120054-8",
            "CAUU 130103-4",
            "CAUU 120057-4",
            "CAUU 120053-2",
            "CAUU 114129-7",
            "CAUU 120081-0",
            "CAUU 120056-9",
            "CAUU 120083-0",
            "CAUU 120052-7",
            "CAUU 120047-1",
            "CAUU 120079-0",
            "CAUU 120049-2",
            "CAUU 120084-6",
            "CAUU 120050-6",
            "CAUU 014224-1",
            "CAUU 120060-9",
            "CAUU 014212-8",
            "CAUU 120085-1",
            "CAUU 014211-2",
            "CAUU 130066-0",
            "CAUU 130087-1",
            "CAUU 130089-2",
            "MSUU 100096-8",
            "MSUU 100024-8",
            "MSUU 100091-0",
            "TITU 082066-7",
            "CPWU 101463-2",
            "CPWU 101465-3",
            "TCIU 454129-1",
            "FCBU 857528-5",
            "FCBU 857527-0",
            "TITU 849154-7",
            "TITU 842176-6",
            "TITU 849131-5",
            "TITU 352595-8",
            "DCSU 105436-0",
            "CAUU 014230-2",
            "TITU 391540-5",
            "TITU 343014-8",
            "CTXU 705518-3",
            "CTXU 705508-0",
            "CTXU 705527-0",
            "FCBU 858079-0",
            "FCBU 857855-6",
            "CAUU 014232-3",
            "CTXU 705534-7",
            "CTXU 705520-2",
            "CTXU 705517-8",
            "CTXU 705542-9",
            "CTXU 705543-4",
            "CTXU 705512-0",
            "CTXU 705511-5",
            "CTXU 705516-2",
            "CTXU 705535-2",
            "CTXU 705506-0",
            "MSUU 500999-0",
            "MSUU 500443-1",
            "FCBU 380521-4",
            "FCBU 018357-0",
            "MSUU 883003-6",
            "MSUU 883001-5",
            "HNKU 513911-3",
            "MSUU 283014-7",
            "XINU 103370-9",
            "MSUU 284007-9",
            "FCBU 857345-1",
            "CAUU 130074-2",
            "TITU 825191-0",
            "TCIU 089040-1",
            "TCIU 086058-3",
            "TCIU 085122-0",
            "TCIU 085115-4",
            "FCBU 857315-3",
            "TGHU 465441-3",
            "PCIU 822001-7",
            "MSUU 283016-8",
            "HLXU 634069-5",
            "HLXU 632964-9",
            "MSUU 381051-0",
            "UACU 331590-3",
            "FCBU 857309-2",
            "FCBU 780630-6",
            "HNKU 511051-0",
            "TITU 826142-0",
            "MSUU 512090-9",
            "MSUU 509690-5",
            "MSUU 508241-3",
            "MSUU 504698-8",
            "MSUU 507159-5",
            "CXIU 380144-5",
            "UACU 809520-5",
            "TITU 825666-1",
            "TITU 826357-3",
            "MSUU 263045-2",
            "GESU 207318-7",
            "GLDU 224602-0",
            "CLHU 883743-8",
            "TITU 352051-3",
            "TITU 363557-5",
            "TCIU 842220-1",
            "TITU 829000-7",
            "MSUU 281289-0",
            "HNKU 513863-1",
            "TITU 391929-4",
            "TITU 391918-6",
            "MSUU 281286-3",
            "TRLU 320617-9",
            "TITU 352944-4",
            "TITU 342926-0",
            "TITU 391510-7",
            "TITU 322211-8",
            "TITU 829361-8",
            "TITU 829330-4",
            "FCBU 780651-7",
            "UACU 504272-6",
            "ECMU 116074-1",
            "FCBU 858354-7",
            "FCBU 857702-0",
            "HLXU 306230-6",
            "HLXU 632901-6",
            "KKFU 730878-1",
            "FCBU 857339-0",
            "FCBU 857307-1",
            "FCBU 857346-7",
            "FCBU 857524-3",
            "CPWU 102500-4",
            "CPWU 102238-7",
            "CPWU 102501-0",
            "CPWU 102239-2",
            "TGHU 233550-4",
            "CPWU 102502-5",
            "CPWU 102236-6",
            "CPWU 102237-1",
            "CPWU 102503-0",
            "MSUU 883004-1",
            "MSUU 883007-8",
            "MSUU 885037-2",
            "MSUU 885038-8",
            "TGHU 482121-2",
            "MSUU 284008-4",
            "TCIU 893013-6",
            "DCSU 353543-1",
            "FCBU 857341-0",
            "FCBU 857525-9",
            "MSUU 481033-0",
            "FCBU 857521-7",
            "FCBU 780647-7",
            "FCBU 780565-5",
            "HLXU 625874-0",
            "HLXU 312317-1",
            "UACU 811768-6",
            "HLXU 313895-2",
            "MSUU 886009-3",
            "MSUU 886011-2",
            "MSUU 886013-3",
            "MSUU 886016-0",
            "MSUU 274012-5",
            "TGHU 460125-0",
            "GLDU 227288-4",
            "FAMU 271470-9",
            "FCBU 018355-0",
            "UACU 500026-9",
            "WLNU 490177-1",
            "FCBU 858315-1",
            "MOTU 072155-2",
            "MSUU 581057-1",
            "CPWU 102382-4",
            "HLXU 630454-8",
            "MSUU 186053-2",
            "MSUU 186049-2",
            "MSUU 186050-6",
            "MSUU 186051-1",
            "MSUU 185034-4",
            "MSUU 185036-5",
            "MSUU 284012-4",
            "MSUU 183018-4",
            "MSUU 183019-0",
            "MSUU 283015-2",
            "MSUU 284001-6",
            "TCIU 842180-1",
            "FCBU 018356-5",
            "FCBU 018367-3",
            "CAUU 114123-4",
            "FCBU 780645-6",
            "MSUU 284006-3",
            "FCBU 018360-5",
            "TRLU 202507-3",
            "FCBU 181023-2",
            "MSUU 284009-0",
            "MSUU 481032-4",
            "TGHU 255278-9",
            "MSUU 481034-5",
            "MSUU 481037-1",
            "MSUU 285032-8",
            "MSUU 285030-7",
            "MSUU 285022-5",
            "FCBU 520532-7",
            "FCBU 520529-2",
            "FCBU 520521-9",
            "FCBU 520516-3",
            "FCBU 520508-1",
            "TDRU 468327-3",
            "TDTU 468602-7",
            "TDRU 469424-1",
            "TDRU 469390-2",
            "MSUU 581056-6",
            "FCBU 857094-0",
            "MSUU 481039-2",
            "MSUU 481029-0",
            "MSUU 481038-7",
            "MSUU 481040-6",
            "CLHU 843366-8",
            "FAMU 271473-5",
            "FAMU 271474-0",
            "MSUU 481036-6",
            "FAMU 271441-6",
            "MSUU 581059-2",
            "FAMU 271429-4",
            "MSUU 581053-0",
            "MSUU 481031-9",
            "HLXU 630809-7",
            "MSUU 285031-2",
            "FAMU 271463-2",
            "FCBU 858309-0",
            "ICOU 110656-7",
            "TCIU 105176-8",
            "FAMU 271456-6",
            "FCBU 018228-1",
            "FAMU 271433-4",
            "MSUU 285039-6",
            "FCBU 858344-4",
            "MSUU 284002-1",
            "CAUU 120059-5",
            "MSUU 284004-2",
            "FCBU 520493-2",
            "UACU 335338-0",
            "FCBU 857098-2",
            "TGHU 254381-1",
            "MSUU 284020-6",
            "CAUU 130102-9",
            "FCBU 857093-5",
            "SHOW 062543-0",
            "FCBU 857308-7",
            "FCBU 857136-1",
            "FCBU 857092-0",
            "MSUU 284005-8",
            "DCSU 390325-6",
            "HLXU 305077-4",
            "GESU 511030-3",
            "PCIU 819487-5",
            "FCBU 018298-0",
            "DCSU 390329-8",
            "TRLU 645669-6",
            "MSUU 161834-4",
            "TCIU 454667-3",
            "TCIU 454009-0",
            "TCIU 370961-0",
            "CPWU 209973-7",
            "MSUU 284003-7",
            "TITU 992533-4",
            "CAUU 130097-4",
            "FCBU 018244-5",
            "WSCU 928839-0",
            "DCSU 370172-2",
            "CCCU 400111-9",
            "MSUU 281158-0",
            "DCSU 370175-9",
            "MSUU 287045-3",
            "DCSU 390326-1",
            "DCSU 390317-4",
            "HHAU 123456-8",
            "BOIU 110024-1",
            "DCSU 370174-3",
            "DCSU 390322-0",
            "DCSU 370171-7",
            "DCSU 390316-9",
            "MSUU 281178-5",
            "DCSU 390320-9",
            "MSUU 281173-8",
            "DCSU 390328-2",
            "CAUU 120087-2",
            "FCBU 857893-6",
            "FAMU 271346-7",
            "MSUU 274014-6",
            "CAUU 130077-9",
            "CAUU 130083-0",
            "BOIU 391043-1",
            "CDKU 287069-1",
            "TGHU 272082-0",
            "CPWU 182382-7",
            "CPWU 182383-2",
            "CPWU 182389-5",
            "FCBU 018257-4",
            "MSUU 581002-0",
            "TITU 363631-3",
            "CXIU 380069-1",
            "FAMU 271342-5",
            "MSUU 274009-0",
            "MSUU 274010-4",
            "MSUU 274011-0",
            "FBXU 020012-2",
            "FCBU 951782-7",
            "FCBU 951776-6",
            "FCBU 951792-0",
            "FCBU 951795-6",
            "FCBU 951762-1",
            "FCBU 018280-4",
            "FCBU 951785-3",
            "FCBU 951773-0",
            "FCBU 951781-1",
            "FCBU 951793-5",
            "DCSU 340727-7",
            "DCSU 340736-4",
            "FCBU 951783-2",
            "DCSU 370162-0",
            "DCSU 385216-4",
            "DCSU 340741-0",
            "DCSU 340730-1",
            "FCBU 951796-1",
            "FCBU 951758-1",
            "DCSU 370161-4",
            "FCBU 951774-5",
            "FCBU 951778-7",
            "FCBU 951777-1",
            "FCBU 951760-0",
            "DCSU 370163-5",
            "DCSU 370164-0",
            "FCBU 018278-5",
            "FCBU 951769-0",
            "DCSU 385218-5",
            "DCSU 385219-0",
            "DCSU 385220-4",
            "DCSU 385212-2",
            "DCSU 385215-9",
            "DCSU 370166-1",
            "DCSU 385217-0",
            "FAMU 070003-3",
            "DCSU 459057-8",
            "DCSU 459065-0",
            "RALU 710456-4",
            "DCSU 459064-4",
            "FBXU 019973-6",
            "DCSU 353587-4",
            "DCSU 459063-9",
            "DCSU 459058-3",
            "DCSU 370167-7",
            "DCSU 390315-3",
            "DCSU 459060-2",
            "DCSU 459056-2",
            "DCSU 459061-8",
            "FBXU 019947-0",
            "GESU 462636-3",
            "GESU 443212-0",
            "PONU 080884-3",
            "GSTU 339004-2",
            "FBXU 019980-2",
            "MSUU 581021-0",
            "MSUU 581001-5",
            "FAMU 070004-9",
            "BOIU 391046-8",
            "FAMU 070018-3",
            "FAMU 070013-6",
            "MSUU 220272-6",
            "FBXU 019936-1",
            "FAMU 070015-7",
            "BMOU 554082-6",
            "MSUU 681002-5",
            "MSUU 681001-0",
            "DCSU 353616-6",
            "DCSU 353598-2",
            "TCIU 105110-9",
            "FXLU 905917-7",
            "FXLU 905885-9",
            "FXLU 905816-5",
            "DCSU 353619-2",
            "CRXU 405458-0",
            "DCSU 353613-0",
            "DCSU 353608-4",
            "UACU 812479-3",
            "GSTU 645128-6",
            "FXLU 905913-5",
            "FXLU 905903-2",
            "FXLU 905902-7",
            "FXLU 905887-0",
            "FXLU 905924-3",
            "FXLU 905886-4",
            "FXLU 905883-8",
            "MAEU 569035-4",
            "GESU 422085-7",
            "MSUU 171056-9",
            "SHOW 925133-0",
            "GBCU 771669-5",
            "UACU 332177-9",
            "TITU 340206-4",
            "MSUU 171063-5",
            "CRXU 494664-8",
            "MSUU 179012-1",
            "RAVU 315309-3",
            "RAVU 550585-0",
            "RAVU 550568-1",
            "RAVU 550580-3",
            "RAVU 550584-5",
            "RAVU 550403-1",
            "RAVU 550436-6",
            "RAVU 550437-1",
            "RAVU 550424-2",
            "RAVU 550432-4",
            "RAVU 315311-2",
            "RAVU 315307-2",
            "RAVU 315310-7",
            "RAVU 315306-7",
            "TDRU 547471-9",
            "INBU 528052-0",
            "DCSU 385203-5",
            "FAMU 270713-0",
            "FAMU 270691-4",
            "FAMU 270682-7",
            "DCSU 390309-2",
            "CTXU 705509-6",
            "OCVU 796343-3",
            "CTXU 705510-0",
            "RALU 710217-6",
            "TITU 829350-0",
            "CTXU 705507-5",
            "CTXU 705519-9",
            "CTXU 705513-6",
            "CTXU 705515-7",
            "CTXU 705514-1",
            "TITU 352903-8",
            "TCIU 453155-0",
            "TCIU 454850-5",
            "TCIU 454879-0",
            "TCIU 454895-3",
            "TCIU 454897-4",
            "BOIU 290014-6",
            "DCSU 385192-8",
            "DCSU 390282-0",
            "TCIU 454382-2",
            "SHOW 215439",
            "DCSU 390283-5",
            "SHOW 220462",
            "FCBU 853605-7",
            "FCBU 853599-7",
            "FCBU 853595-5",
            "FCBU 853588-9",
            "FCBU 852275-2",
            "MSUU 220976-2",
            "MSUU 531012-8",
            "BKXU 707097-5",
            "TEST 123456",
            "MTBU 207753-6",
            "MSUU 220336-3",
            "MSUU 220239-3",
            "MSUU 220174-0",
            "HJCU 209368-5",
            "MWCU 651388-1",
            "FCBU 951083-8",
            "MTBU 040728-5",
            "MSOU 025002-4",
            "MSOU 025001-9",
            "TRIU 024991-1",
            "AMFU 500197-6",
            "TRIU 836747-0",
            "TRLU 169409-5",
            "RALU 710121-0",
            "SHOW 401174?5",
            "DCSU 402353?8",
            "CLHU 275777-8",
            "CLHU 215947-8",
            "TTNU 307628-7",
            "TRXU 494959-8",
            "FBXU 018994-9",
            "MWCU 564356-1",
            "AMFU 303421-9",
            "CLHU 295819-7",
            "GATU 092809-8",
            "MAXU 241728-1",
            "TGHU 277180-6",
            "AMFU 305402-5",
            "CLHU 314997-8",
            "GATU 107073-7",
            "COIU 100064-4",
            "MSUU 241145-4",
            "TCIU 842182-2",
            "TCLU 621283-3",
            "TGHU 453213-8",
            "CLHU 443851-1",
            "TGHU 438645-5",
            "UACU 811100-8",
            "FCBU 950080-3",
            "CLHU 448336-2",
            "MSUU 500866-9",
            "TITU 990305-8",
            "FXLU 905920-1",
            "DCSU 452928?6",
            "TCKU 974234-1",
            "CCLU 665049-5",
            "TCNU 921599-9",
            "PCIU 815105-0",
            "HNKU 513436-4",
            "CTXU 347237-9",
            "CTXU 347284-6",
            "CTXU 347276-4",
            "CTXU 347234-2",
            "CTXU 347232-1",
            "CTXU 347286-7",
            "CTXU 347282-5",
            "CTXU 347233-7",
            "CTXU 347278-5",
            "PCIU 827445-6",
            "PCIU 829037-5",
            "GESU 505470-3",
            "PCIU 817397-5",
            "HJLU 700918-5",
            "TGTU 800002-2",
            "PCIU 820850-0",
            "HJLU 700917-0",
            "RALU 711194-3",
            "TITU 925904-8",
            "MSUU 506661-8",
            "MOTU 581050-2",
            "CAIU 889131-5",
            "SEGU 477232-0",
            "TCLU 654507-4",
            "GLDU 762724-6",
            "CMAU 572815-9",
            "TCIU 842192-5",
            "TCIU 842097-6",
            "TCIU 842095-5",
            "CPWU 290038-4",
            "TCIU 421614-1",
            "TCIU 842186-4",
            "TITU 842175-0",
            "TCIU 842194-6",
            "TITU 321039-6",
            "BXRU 421013-9",
            "TITU 842173-0",
            "FCBU 857064-2",
            "CPWU 215741-1",
            "CPWU 215721-6",
            "CPWU 215715-5",
            "CPWU 208897-0",
            "CDDU 901342-1",
            "FCBU 849540-4",
            "CXIU 380102-3",
            "TITU 825794-5",
            "TITU 825142-2",
            "CNSU 200347-7",
            "CPWU 208904-5",
            "CPWU 208898-5",
            "MSUU 022002",
            "TCLU 448006-1",
            "GLDU 063416-1",
            "FCBU 857070-3",
            "TGHU 387724-3",
            "TITU 569214-7",
            "CCLU 628225-9",
            "MSUU 220247-5",
            "CPWU 215759-8",
            "CLHU 332900-7",
            "UACU 324618-7",
            "CPWU 215717-6",
            "CPWU 215758-2",
            "TCLU 880294-7",
            "FCBU 520534-8",
            "TCNU 928559-5",
            "CAUU 114127-6",
            "HDMU 636268-9",
            "AMFU 866247-6",
            "WSCU 802975-3",
            "TEMU 698328-9",
            "HBTU 208266-0",
            "CAIU 260922-0",
            "TCIU 420984-1",
            "NEWU 200853-0",
            "CAUU 120078-5",
            "TCIU 590235-7",
            "HLXU 502672-0",
            "TCIU 811794-9",
            "TCIU 810014-4",
            "FXLU 905934-6",
            "NEVU 796152-0",
            "MSUU 151047-3",
            "MSCU 016680-5",
            "MOFU 046048-0",
            "CPWU 390473-8",
            "FCIU 266225-9",
            "BOIU 391302-4",
            "TITU 391734-7",
            "ECMU 123810-9",
            "ECMU 116061-2",
            "CPIU 556059-3",
            "DNCU 820068-3",
            "FSCU 600159-6",
            "TCIU 452213-6",
            "CTXU 347238-4",
            "HHAU 645789?0",
            "CPLU 200188-9",
            "JXLU 592267-9",
            "MTBU 211584-7",
            "DCSU 359013-0",
            "RALU 410176-7",
            "FCBU 856976-5"
    };


    static {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            TEST_IDS.addAll(Arrays.asList(ALTER_IDS));
    }

    public static void main(String[] args) {
        AIMainOCR ai = new AIMainOCR("aidataFinal.dat", 24,24);
//        File[] files = new File(fromPathP).listFiles();
//        assert files != null;
//        Arrays.stream(files).forEach(file -> {
//            OCRBridge(ai, file);
//        });

        Mat m = Imgcodecs.imread("res/03.png");
        MatOfByte mb = new MatOfByte();
        Imgcodecs.imencode(".png", m, mb);
        System.out.println(OCRBridge(ai,mb, 13)[1]);

    }

    public static String[] OCRBridge(AIMainOCR ai, MatOfByte imageByte, int blocksize) {
        MatOfByte[] imageBytes = Segmentation.segment(imageByte, blocksize, false);
        MatOfByte[] inversedBytes = Segmentation.segment(imageByte, blocksize, true);

        AIGuess normalGuesses = doGuess(imageBytes, ai );
        AIGuess inverseGuesses = doGuess(inversedBytes, ai);

        //System.out.println("Normal%: " + normalGuesses.quality + " || " + "Inversed%: " + inverseGuesses.quality);
        if (normalGuesses.quality > inverseGuesses.quality){

            //System.out.println(String.format("%s || %s || %s", normalGuesses.guesses[0], normalGuesses.guesses[1],normalGuesses.guesses[2]));

            ArrayList<ArrayList<Integer>> lstein = Levenshtein.levenshteinDistance(normalGuesses.guesses, TEST_IDS.toArray(new String[]{}));
            String bestGuessFromAI = normalGuesses.guesses[lstein.get(0).get(0)];
            String bestGuessWithLS = TEST_IDS.get(lstein.get(1).get(0));

//            System.out.println(String.format("Best guess: %s || With list help: %s", bestGuessFromAI, bestGuessWithLS));
            if (lstein.get(1).size() > 1) {
                for (int k : lstein.get(1)) {
                    //System.out.println("EQUAL GUESSES WITH LS: " + TEST_IDS.get(k));
                }
            }
            //System.out.println("=========================================");

            return new String[]{bestGuessFromAI, bestGuessWithLS};

        }else {

            ArrayList<ArrayList<Integer>> lstein = Levenshtein.levenshteinDistance(inverseGuesses.guesses, TEST_IDS.toArray(new String[]{}));
            String bestGuessFromAI = inverseGuesses.guesses[(lstein.get(0).get(0))];
            String bestGuessWithLS = TEST_IDS.get(lstein.get(1).get(0));


            //System.out.println(String.format("INVERSED → %s || %s || %s", inverseGuesses.guesses[0],inverseGuesses.guesses[1],inverseGuesses.guesses[2]));
            //System.out.println(String.format("INVERSED → Best guess %s || With list help: %s", bestGuessFromAI, bestGuessWithLS));
            if (lstein.get(1).size() > 1) {
                for (int k : lstein.get(1)) {
                    //System.out.println("EQUAL GUESSES WITH LS: " + TEST_IDS.get(k));
                }
            }
            //System.out.println("=========================================");

            return new String[]{bestGuessFromAI, bestGuessWithLS};
        }
    }

    private static class AIGuess{
        String[] guesses;
        double quality;

        AIGuess(String[] guesses, double quality){
            this.guesses = guesses;
            this.quality = quality;
        }
    }
    private static AIGuess doGuess(MatOfByte[] imageBytes, AIMainOCR ai) {
        //Array of [Guess1, Guess2, Guess3]
        String[] guesses = new String[]{"","",""};
        double normalPercentage = 0;
        int p = 11 < imageBytes.length ? 11 : imageBytes.length;
        for (int i = 0; i < p; i++) {
            MatOfByte byt = imageBytes[i];
            Mat mat = Imgcodecs.imdecode(byt, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
            Sample sample = AIAsciiSample.loadMatSample(mat, ai.flavors, 24, 24, new Dimension(1, 1));
            Result result = sample.getResult(ai.flavors[0], 5, -1, false);
            Target[] targets = result.targets;
            List<Target> targetList = new ArrayList<>(Arrays.asList(targets));
//            System.out.println("Targets pre-sort: " + targets[0].value + "|" + targets[1].value + "|" + targets[2].value);
            if (i == 4)guesses[0] += " ";
            if (i == 10) guesses[0] += "-";
            if (i < 4) {
                targetList.sort(new TargetComparator(true));
            }
            if (i >= 4 && i < p) {
                targetList.sort(new TargetComparator(false));
            }
            guesses[0] += targetList.get(0).value;
            guesses[1] += targetList.get(1).value;
            guesses[2] += targetList.get(2).value;
            normalPercentage += targets[0].quality;
//            Imgcodecs.imwrite(outputPath +"_"+ name + "_" + denomination + i + ".png", mat); //for testing --add targets[0].value before name for AI training
        }
        normalPercentage /= p;
        return new AIGuess(guesses, normalPercentage);
    }
}
