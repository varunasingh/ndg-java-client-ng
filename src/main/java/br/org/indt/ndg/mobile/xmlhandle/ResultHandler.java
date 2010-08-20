package br.org.indt.ndg.mobile.xmlhandle;

import br.org.indt.ndg.lwuit.model.ImageAnswer;
import br.org.indt.ndg.lwuit.model.NDGAnswer;
import br.org.indt.ndg.mobile.multimedia.Base64Coder;
import java.util.Hashtable;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import br.org.indt.ndg.mobile.structures.ResultStructure;
import br.org.indt.ndg.mobile.structures.answer.Answer;
import br.org.indt.ndg.mobile.structures.answer.BoolAnswer;
import br.org.indt.ndg.mobile.structures.answer.ChoiceAnswer;
import br.org.indt.ndg.mobile.structures.answer.DateAnswer;
import br.org.indt.ndg.mobile.structures.answer.DecimalAnswer;
import br.org.indt.ndg.mobile.structures.answer.IntegerAnswer;
import br.org.indt.ndg.mobile.structures.answer.StringAnswer;
import br.org.indt.ndg.mobile.structures.answer.TimeAnswer;
import java.util.Calendar;

public class ResultHandler extends DefaultHandler {
    private ResultStructure result;
    private Stack tagStack = new Stack();
    private NDGAnswer currentAnswer;
    private Hashtable answers=null;
    
    private String currentOtherIndex = null;
    
    public ResultHandler() {}
    
    public void setResultStructure(ResultStructure _structure) {
        this.result = _structure;
    }
    
    public void startDocument() throws SAXException {
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {        
        if (qName.equals("result")) {
            result.setResultId(attributes.getValue(attributes.getIndex("r_id")));
            result.setSurveyId(Integer.parseInt(attributes.getValue(attributes.getIndex("s_id"))));
            result.setUserId(attributes.getValue(attributes.getIndex("u_id")));
            result.setTimeTaken(Long.parseLong(attributes.getValue(attributes.getIndex("time"))));
        } else if (qName.equals("category")) {
            answers = new Hashtable();
            result.addCatName(attributes.getValue(attributes.getIndex("name")));
            result.addCatId(attributes.getValue(attributes.getIndex("id")));
        } else if (qName.equals("answer")) {
            String _type = attributes.getValue(attributes.getIndex("type"));
            String _id = attributes.getValue(attributes.getIndex("id"));
            String _visted = attributes.getValue(attributes.getIndex("visited"));
            
            if (_type.equals("_str")) currentAnswer = new StringAnswer();
            else if (_type.equals("_date")) currentAnswer = new DateAnswer();
            else if (_type.equals("_time")){               
                currentAnswer = new TimeAnswer();
                String sconvention = attributes.getValue(attributes.getIndex("convention")) ;
                long convention  = 0;
                if(sconvention.equals("am")){
                    convention = 1;
                }else if(sconvention.equals("pm")){
                    convention = 2;
                }

                ((TimeAnswer)currentAnswer).setConvention(convention);


            }
            else if (_type.equals("_int")) currentAnswer = new IntegerAnswer();
            else if (_type.equals("_decimal")) currentAnswer = new DecimalAnswer();
            else if (_type.equals("_choice")) currentAnswer = new ChoiceAnswer();
            else if(_type.equals("_img")) currentAnswer = new ImageAnswer();
            
            currentAnswer.setType(_type);
            currentAnswer.setId(Integer.parseInt(_id));
            currentAnswer.setVisited(_visted);
        } else if (qName.equals("other")) {
            currentOtherIndex = attributes.getValue(0);
        }
             
        tagStack.push(qName);
    }
    private long timeStamp2Long(String time,long convention){
        int ix = time.indexOf(':');
        int hour = Integer.parseInt(time.substring(0,ix));
        int min  = Integer.parseInt(time.substring(ix+1));
        Calendar calendar = Calendar.getInstance();
        if(convention == 0){           
           calendar.set(Calendar.HOUR_OF_DAY, hour);
        }else{            
            calendar.set(Calendar.HOUR, hour);
        }
        calendar.set(Calendar.MINUTE, min);

        
        return calendar.getTime().getTime();
    }
    public void characters(char[] ch, int start, int length) throws SAXException {
        String chars = new String(ch, start, length).trim();
        if (chars.length() > 0) {
            String qName = (String)tagStack.peek();
            
            if (qName.equals("str")) ((StringAnswer) currentAnswer).setValue(chars);
            else if (qName.equals("date")) ((DateAnswer) currentAnswer).setDate(Long.parseLong(chars));
            else if (qName.equals("time")) ((TimeAnswer) currentAnswer).setTime(timeStamp2Long(chars,((TimeAnswer) currentAnswer).getConvention()));
            else if (qName.equals("int")) {
                int iValue = 0;
                try {
                    iValue = Integer.parseInt(chars);
                }
                catch(NumberFormatException ex) {}
                ((IntegerAnswer) currentAnswer).setValue(iValue);
            }
            else if (qName.equals("decimal"))
                ((DecimalAnswer) currentAnswer).setValue(Double.parseDouble(chars));                
            else if (qName.equals("index")) ((BoolAnswer) currentAnswer).setIndex(chars);
            else if (qName.equals("item")){
                ((ChoiceAnswer) currentAnswer).setSelectedIndex(chars);                
            }
            else if (qName.equals("other")) {
                ((ChoiceAnswer) currentAnswer).setSelectedIndex(currentOtherIndex);
                ((ChoiceAnswer) currentAnswer).setOtherText(currentOtherIndex, chars);
            }
            else{
                //ATTENTION
                //This "else" means that is a new type of Answer from a new type
                //of Question
                //USE Answer that extends br.org.indt.ndg.lwuit.model.Answer
                //using its setValue()
                //This is necessary from version 2.0 on.
                if (qName.equals("img_data")){
                    if(chars != null && chars.length() > 0 && !chars.equals(" ") )
                    ((ImageAnswer) currentAnswer).setValue(Base64Coder.decode(chars));
                }
            }
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        
        if (qName.equals("category")) {
            result.addCategory(answers);
        } else if (qName.equals("answer")) {
            answers.put(String.valueOf(currentAnswer.getId()), currentAnswer);
        }
        
        tagStack.pop();
    }
    
    public void endDocument() throws SAXException {}
    
}