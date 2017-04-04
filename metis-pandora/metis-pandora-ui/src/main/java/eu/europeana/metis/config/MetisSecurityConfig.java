/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import eu.europeana.metis.ui.mongo.domain.Roles;

/**
 * This configuration sets up Metis web pages LDAP authorization.
 * @author alena
 *
 */
@Configuration
@EnableWebSecurity
public class MetisSecurityConfig extends WebSecurityConfigurerAdapter {

	@Configuration
	@PropertySource("classpath:authentication.properties")
	protected static class AuthenticationConfiguration extends GlobalAuthenticationConfigurerAdapter {
		
		@Value("${ldap.url}")
		private String url;
		
		@Value("${ldap.manager.dn}")
		private String managerDN;
		
		@Value("${ldap.manager.pwd}")
		private String managerPWD;
		
		@Override
		public void init(AuthenticationManagerBuilder auth) throws Exception {		
			auth.ldapAuthentication()
			.contextSource()
			.url(url).managerDn(managerDN).managerPassword(managerPWD) 
			.and()
            .userSearchBase("ou=users,ou=metis_authentication")
            .userSearchFilter("(uid={0})")
            .groupSearchBase("ou=roles,ou=metis_authentication")
            .groupRoleAttribute("cn")
            .groupSearchFilter("(member={0})");
		}
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
			http.authorizeRequests()
					.antMatchers("/").permitAll()
					.antMatchers("/profile")
						.hasAnyRole("EUROPEANA_ADMIN","EUROPEANA_VIEWER", "EUROPEANA_DATA_OFFICER", "HUB_ADMIN", "HUB_VIEWER", "HUB_DATA_OFFICER")
						.anyRequest().authenticated().anyRequest().permitAll()
					.antMatchers("/profile").authenticated()
					.antMatchers("/register").permitAll()
					.antMatchers("/mappings-page").permitAll() //TODO the mapping page is public for now only for test reasons
					.antMatchers("/requests").hasRole(Roles.EUROPEANA_ADMIN.name()).anyRequest().authenticated()
					.and()
				.logout()
					.logoutSuccessUrl("/login").permitAll()
					.and()
				.formLogin().loginProcessingUrl("/login")
						.loginPage("/login").defaultSuccessUrl("/")
						.failureUrl("/login?authentication_error=true").permitAll()
					.and()
				.csrf().disable();
			// @formatter:on
	}
}
