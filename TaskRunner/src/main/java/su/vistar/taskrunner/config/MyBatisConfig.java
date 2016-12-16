package su.vistar.taskrunner.config;
import org.apache.commons.dbcp.BasicDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@MapperScan("su.vistar.taskrunner.mapper")
public class MyBatisConfig {
    @Bean
    public BasicDataSource dataSource() {
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");           
            dataSource.setUsername("dasha");
            //"jdbc:mysql://vps2.vistar.su:3306"
            dataSource.setUrl("jdbc:mysql://vps1.vistar.su:3306/tryasometr_v2");
            dataSource.setPassword("dasha");
            return dataSource;
    }
    @Bean
    public DataSourceTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
    }
    
    @Bean
    public SqlSessionFactoryBean sqlSessionFactory() throws Exception {
            SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
            sessionFactory.setDataSource(dataSource());         
            return sessionFactory;
    }
   
}
