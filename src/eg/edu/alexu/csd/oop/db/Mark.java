package eg.edu.alexu.csd.oop.db;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *          to call this class
 *          use it
 *          Mark m = new Mark();
 *          m.GetData("   EXPRESSION ,  TABLE )
 *          it return  a LinkedList of object
 *
 */
class Mark {

    private String[] column;
    private Class[] Types;
    private Object[] Data;
    private String Expression ;
    private Boolean allowForData;
    private Boolean allowForOperation;

    private Object[] operation;

    private char[] primaryOperation ={'+','-','*','/'};

    private char[] AllowedChar ={'+','-','*','/','(',')','<','>','='};

    private  Stack<Object> OperationStack=new Stack<>();
    private Stack<Object> DataStack=new Stack<>();

    LinkedList<Object[]> getData(String Express, Table table) throws SQLException {

        LinkedList<Object[]> correct=new LinkedList<>();
        String TempExpreetion=Spaces(Express);

        LinkedList<Object[]> Data = table.getTable();

        column=table.getHeaders();
        Types=table.getTypes();

        for (Object[] datum : Data) {
            ResetData();

            this.Data = datum;
            Expression = TempExpreetion;

            Mark();
            operation = new getExpression().GetExpression(Expression);
            Calc();

            if ((DataStack.peek().getClass() == boolean.class || DataStack.peek().getClass() == Boolean.class) && ((boolean) DataStack.peek())) {
                correct.add(datum);
            }
        }
        return  correct;
    }

    private void ResetData() {
        allowForData=true;
        allowForOperation=false;
        OperationStack=new Stack<>();
        DataStack=new Stack<>();
    }

    private String Spaces(String express) {
        StringBuilder s = new StringBuilder(" ");
        int numberOfPranthes=0;

        for(int i=0;i<express.length();i++){
            if(express.charAt(i)=='\"'){
                numberOfPranthes++;
                s.append("\"");
                if(numberOfPranthes%2==0){
                    s.append(" ");
                }
                continue;
            }
            if(numberOfPranthes%2==1){
                s.append(express.charAt(i));
                continue;
            }
            boolean b=false;
            for(char c:AllowedChar){
                if(c==express.charAt(i)){
                    s.append(" ");
                    s.append(express.charAt(i));
                    s.append(" ");
                    b=true;
                    break;
                }

            }
            if(b)continue;
            s.append(express.charAt(i));

        }
        s.append(" ");
        return s.toString();
    }


    private void Calc() {
        for (Object o : operation) {
            Distibute(o);
        }
        DeathStack();
    }

    private void Distibute(Object o)
    {
        if(PrimaryOperation(o)||comparitionOperation(o))
        {
            AppendOperation(o);
        }else if(logicOperation(o)){
            AppendLogic(o);
        } else if(TRUEFALSE(o)){
            RemoveNot((Boolean.parseBoolean(o.toString())));
            allowForData=false;
            allowForOperation=true;
        }else if(o.getClass()==Character.class&&(char)o=='('){
            if(allowForData){
                OperationStack.push(o);
            }else throw new NumberFormatException();
        }else if(o.getClass()==Character.class&&(char)o==')'){
            DeathPrantetes();
            allowForOperation=true;
            allowForData=false;
        }
        else  {
            if(allowForData)
            {
                DataStack.push(o);
                allowForData=false;
                allowForOperation=true;
            }
            else{
                throw new NumberFormatException();
            }
        }
    }

    private void DeathPrantetes() {
        while (!OperationStack.empty()&&(OperationStack.peek().getClass()!=Character.class||(char)OperationStack.peek()!='(')){
            Object o=OperationStack.peek();
            if(PrimaryOperation(o))RemovePrimaryOperation();
            else if(comparitionOperation(o))RemoveComparition();
            else  if(o.equals("and"))RemoveAnd();
            else if(o.equals("or")){
                RemoveOR();
            }
            else  throw new NumberFormatException();

        }

        if((char)OperationStack.peek()!='('){
            throw new NumberFormatException();
        }
        OperationStack.pop();
        if(!OperationStack.empty()&&OperationStack.peek().equals("not")){
            if(DataStack.peek().getClass()==Boolean.class)
                RemoveNotDeath();
            else throw new NumberFormatException();
        }
    }

    private boolean TRUEFALSE(Object o) {
        if(o.getClass()!=String.class){
            return false;
        }
        String str=(String)o;
        return str.equalsIgnoreCase("true")||str.equalsIgnoreCase("false");
    }

    private void AppendLogic(Object o) {
        String str=(String)o;
        if(str.equalsIgnoreCase("and")){
            if(!allowForOperation)throw new NumberFormatException();
            allowForOperation=false;
            allowForData=true;
            while (DataStack.peek().getClass()!=Boolean.class&&PrimaryOperation(OperationStack.peek())){
                RemovePrimaryOperation();
            }
            RemoveComparition();
            OperationStack.push("and");

        }else if (str.equalsIgnoreCase("or")){
            if(!allowForOperation)throw new NumberFormatException();
            allowForOperation=false;
            allowForData=true;

            while (DataStack.peek().getClass()!=Boolean.class&&PrimaryOperation(OperationStack.peek())){
                RemovePrimaryOperation();
            }
            RemoveComparition();
            while (!OperationStack.empty()&&OperationStack.peek().equals("and")){
                RemoveAnd();
            }
            OperationStack.push("or");

        }
        else {
            allowForOperation=false;
            allowForData=true;
            if(!OperationStack.empty()&&OperationStack.peek().equals("not")){
                OperationStack.pop();
                return;
            }
            OperationStack.push("not");
        }
    }



    private void RemoveComparition() {
        if(DataStack.peek().getClass()==Boolean.class)return;
        Object y=DataStack.pop();
        Object x=DataStack.pop();
        char C=(char)OperationStack.pop();
        switch (C){
            case '>':
                if(isString(y,x)){
                    RemoveNot(((String)x).toLowerCase().compareTo(((String)y).toLowerCase())>0);
                }else {
                    RemoveNot((int)x>(int)y);
                }
                break;
            case '<':
                if(isString(y,x)){
                    RemoveNot(((String)x).toLowerCase().compareTo(((String)y).toLowerCase())<0);
                }else {
                    RemoveNot((int)x<(int)y);
                }
                break;
            case '=':
                if(isString(y,x)){
                    RemoveNot(((String)x).toLowerCase().compareTo(((String)y).toLowerCase())==0);
                }else {
                    RemoveNot((int)x==(int)y);
                }
                break;

        }
    }
    private void RemoveAnd() {
        boolean x=(boolean)DataStack.pop();
        boolean y=(boolean)DataStack.pop();
        OperationStack.pop();
        RemoveNot(x&&y);
    }

    private void RemoveNot(boolean b){

        if(!OperationStack.empty()&&OperationStack.peek().equals("not")){
            OperationStack.pop();
            DataStack.push(!b);
            return;
        }
        DataStack.push(b);
    }
    private void RemoveOR() {
        boolean x=(boolean)DataStack.pop();
        boolean y=(boolean)DataStack.pop();
        OperationStack.pop();
        RemoveNot(x||y);
    }

    private boolean isString(Object o , Object o1){
        if(o.getClass()==String.class){
            if(o1.getClass()==String.class){
                return true;
            }
            throw new NumberFormatException();
        }
        if(o.getClass()==Integer.class){
            if(o1.getClass()==Integer.class){
                return false;
            }
            throw new NumberFormatException();
        }
        throw new NumberFormatException();
    }

    private boolean logicOperation(Object o) {
        return (o.getClass()==String.class&&(((String)o).toLowerCase().equals("and")||
                ((String)o).toLowerCase().equals("or")||
                ((String)o).toLowerCase().equals("not")
        ));
    }

    private boolean comparitionOperation(Object o) {
        if(o.getClass()!=Character.class)return false;
        return (char) o == '>' || (char) o == '<' || (char) o == '=';
    }

    private void AppendOperation( Object object) {
        if(!allowForOperation)throw new NumberFormatException();
        allowForOperation=false;
        allowForData=true;

        if(OperationStack.empty()||!PrimaryOperation(OperationStack.peek())){
            OperationStack.push(object);
            return;
        }

        if(((char)object=='*'||(char)object=='/'))
            while (!OperationStack.empty()&&((char)OperationStack.peek()=='*'||(char)OperationStack.peek()=='/'))
                RemovePrimaryOperation();
        else if((char)object=='+'||(char)object=='-'||(char)object=='>'||(char)object=='<'||(char)object=='='){
            while (!OperationStack.empty()&&PrimaryOperation(OperationStack.peek()))
                RemovePrimaryOperation();
        }
        OperationStack.push(object);
    }

    private void RemovePrimaryOperation() {
        int y=(int)DataStack.pop();
        int x=(int)DataStack.pop();
        char c =(char)OperationStack.pop();
        switch (c){
            case '+':
                DataStack.push(x+y);
                break;
            case '-':
                DataStack.push(x-y);
                break;
            case '*':
                DataStack.push(x*y);
                break;
            case '/':
                DataStack.push(x/y);
                break;
        }


    }
    private boolean PrimaryOperation(Object object)
    {
        for(char c : primaryOperation) {
            try {
                if (c == (char) object) return true;
            }catch (Exception ignored){}
        }
        return false;
    }
    private void DeathStack(){
        // at the end of expression to make tow stacks empty
        while (!OperationStack.empty()){
            Object o=OperationStack.peek();
            if(PrimaryOperation(OperationStack.peek()))RemovePrimaryOperation();
            else if(comparitionOperation(o))RemoveComparition();
            else  if((OperationStack.peek()).equals("and"))RemoveAnd();
            else if((OperationStack.peek()).equals("or"))RemoveOR();
            else if((OperationStack.peek()).equals("not"))RemoveNotDeath();
            else throw new NumberFormatException();
        }
        if(DataStack.empty()||DataStack.peek().getClass()!=Boolean.class)
            throw new NumberFormatException();
    }

    private void RemoveNotDeath() {
        boolean b=(boolean)DataStack.pop();
        OperationStack.pop();
        DataStack.push(!b);
    }

    private void Mark()
    {
        for(int i=0;i<column.length;i++)
        {
            if(Types[i]==String.class){
                boolean x = Replace(" "+column[i].toLowerCase(),((String) Data[i]).toLowerCase());
                if(x)i--;
            }else {
                boolean x = Replace(" "+column[i],String.valueOf(Data[i]));
                if(x)i--;
            }
        }
    }
    private boolean Replace(String ReplaceIt,String ReplaceWith)
    {
        Pattern pattern=Pattern.compile(ReplaceIt.toLowerCase());
        Matcher matcher=pattern.matcher(Expression.toLowerCase());
        if (matcher.find())
        {
            int begin=matcher.start();
            int end=matcher.end();
            boolean FindBefore=before(begin);
            boolean FindAfter=after(end);
            boolean bound=false;
            if(end== Expression.length()|| Expression.charAt(end)==' ')
            {
                bound=true;
            }
            if(FindBefore&&FindAfter&&bound)
            {
                Expression = new StringBuilder(Expression).replace(begin+1,end,ReplaceWith).toString();

            }
            return true;
        }
        return false;
    }

    private boolean after(int x) {
        for(int i = x; i< Expression.length(); i++){
            if(Expression.charAt(i)==' ')continue;
            return Expression.charAt(i) != '\"';
        }
        return true;
    }

    private boolean before(int x){
        for(int i=x-1;i>=0;i--){
            if(Expression.charAt(i)==' ')continue;
            return Expression.charAt(i) != '\"';
        }
        return true;
    }
}
