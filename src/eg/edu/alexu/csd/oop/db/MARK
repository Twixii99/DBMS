package eg.edu.alexu.csd.oop.db;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mark {
    String column[]={"name" , "age" , "email" ,"success"};
    Class Types[]={String.class,int.class,String.class,boolean.class};
    Object Data[]={"ahmed",15,"a@g.com",true};
    private String Expression =" name = \"ahmed\" And age = 15 or email = \"sfsdafas\" And success";
    private Boolean allowForData=true;
    private Boolean allowForOperation=false;
  //  Object operation[]={'(',5,'-',10,'=',-6,')'};
    Object operation[]={'(','(','(',5,'+',7,'<',3,')',"or",'(','(',6,'-',3,')','*',3,'>',6,')',')',"and",'(',"not",'(',"not",'(',"not",'(',"ahmed",'=',"ahmed",')',')',')',')',')'};
    char primaryOperation[]={'+','-','*','/'};
    Stack OperationStack=new Stack();
    Stack DataStack=new Stack();
    Mark(){
            Mark();
            Calc();
            System.out.println(DataStack.peek());
    }

    private void Calc() {
        for(int i=0;i<operation.length;i++){
            Distibute(operation[i]);
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
               System.out.println(o);
               throw new NumberFormatException();
           }
        }
    }

    private void DeathPrantetes() {
        while (!OperationStack.empty()&&(OperationStack.peek().getClass()!=Character.class||(char)OperationStack.peek()!='(')){
            Object o=OperationStack.peek();
            if(PrimaryOperation(o))RemovePrimaryOperation();
            else if(comparitionOperation(o))RemoveComparition();
            else  if(((String)o).equals("and"))RemoveAnd();
            else if(((String)o).equals("or")){
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
                    RemoveNot(((String)x).compareTo((String) y)>0);
                }else {
                    RemoveNot((int)x>(int)y);
                }
                return;
            case '<':
                if(isString(y,x)){
                    RemoveNot(((String)x).compareTo((String) y)<0);
                }else {
                    RemoveNot((int)x<(int)y);
                }
                return;
            case '=':
                if(isString(y,x)){
                    RemoveNot(((String)x).compareTo((String) y)==0);
                }else {
                    RemoveNot((int)x==(int)y);
                }
                return;

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

    boolean isString(Object o , Object o1){
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
        if((char)o=='>'||(char)o=='<'||(char)o=='=')return true;
        return false;
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
        int y=(int)DataStack.peek();
        DataStack.pop();
        int x=(int)DataStack.peek();
        DataStack.pop();
        char c =(char)OperationStack.peek();
        OperationStack.pop();
        switch (c){
            case '+':
                DataStack.push(x+y);
                return;
            case '-':
                DataStack.push(x-y);
                return;
            case '*':
                DataStack.push(x*y);
                return;
            case '/':
                DataStack.push(x/y);
                return;
        }

    }
    boolean PrimaryOperation(Object object)
    {

        for(char c : primaryOperation) {
            try {
                if (c == (char) object) return true;
            }catch (Exception e){}
        }
        return false;

    }
    void DeathStack(){
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
        Boolean b=(boolean)DataStack.pop();
        OperationStack.pop();
        DataStack.push(!b);
    }


    void Mark()
    {
        System.out.println(Expression);
        for(int i=0;i<column.length;i++)
        {
            if(Types[i]==String.class){
                Replace(" "+column[i],"\""+Data[i]+"\"");
            }else {
                Replace(" "+column[i],String.valueOf(Data[i]));
            }
        }
        System.out.println(Expression);
    }
    private void Replace(String ReplaceIt,String ReplaceWith)
    {
        Pattern pattern=Pattern.compile(ReplaceIt);
        Matcher matcher=pattern.matcher(Expression);
        while (matcher.find())
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
        }
    }

    private boolean after(int x) {
        for(int i = x; i< Expression.length(); i++){
            if(Expression.charAt(i)==' ')continue;
            if(Expression.charAt(i)=='\"')return false;
            return true;
        }
        return true;
    }

    boolean before(int x){
           for(int i=x-1;i>=0;i--){
               if(Expression.charAt(i)==' ')continue;
               if(Expression.charAt(i)=='\"')return false;
               return true;
           }
        return true;
    }
}
