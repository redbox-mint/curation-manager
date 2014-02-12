package au.com.redboxresearchdata.curationmanager.utility

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
 
@Singleton
class ApplicationContextHolder implements ApplicationContextAware {
 
   private ApplicationContext ctx
 
   void setApplicationContext(ApplicationContext applicationContext) {
       ctx = applicationContext
   }
 
   static ApplicationContext getApplicationContext() {
      getInstance().ctx
   }
}
