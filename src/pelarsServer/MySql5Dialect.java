package pelarsServer;

import org.hibernate.dialect.MySQL5InnoDBDialect;
 
import java.sql.Types;
 
public class MySql5Dialect extends MySQL5InnoDBDialect{
 
  public String getTableTypeString(){
    return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
  }
 
  public MySql5Dialect(){
    super();
    registerColumnType(Types.BLOB, "LONGBLOB");
  }
}