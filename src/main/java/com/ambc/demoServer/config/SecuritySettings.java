package com.ambc.demoServer.config;

import com.ambc.demoServer.accessAuthorizations.AccessDeniedHandler;
import com.ambc.demoServer.accessAuthorizations.ForbiddenEntryPoint;
import com.ambc.demoServer.accessAuthorizations.JWTAuthFilter;
import com.ambc.demoServer.tokenSecurity.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configurable
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecuritySettings extends WebSecurityConfigurerAdapter {
    private final JWTAuthFilter jwtAuthFilter;
    private final AccessDeniedHandler accessDeniedHandler;
    private final ForbiddenEntryPoint forbiddenEntryPoint;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public SecuritySettings(JWTAuthFilter jwtAuthFilter,
                            AccessDeniedHandler accessDeniedHandler,
                            ForbiddenEntryPoint forbiddenEntryPoint,
                            @Qualifier("ourUserDetailsService") UserDetailsService userDetailsService,
                            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.accessDeniedHandler = accessDeniedHandler;
        this.forbiddenEntryPoint = forbiddenEntryPoint;
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //for cors .disable() to remove
        http.csrf().disable().cors()
                .and()
                /*
                  No sessions
                  when a user enter the app => login => app give a token
                  everytime after that the client give token, so we can verify
                 */
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //All this urls !== to authenticate
                .authorizeRequests().antMatchers(Constants.publicUrls).permitAll()
                //All the others needs authenticate
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(forbiddenEntryPoint)
                .and()
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
