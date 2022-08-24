package account.security;

import account.exceptions.AccessDeniedHandlerExceptionHandler;
import account.services.SecurityEventsService;
import account.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;

@EnableWebSecurity
public class AuthenticationAdapter extends WebSecurityConfigurerAdapter{

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final SecurityEventsService eventsService;
    private final RestAuthenticationEntryPoint entryPoint;

    public AuthenticationAdapter(PasswordEncoder encoder, UserService service, SecurityEventsService eventsService, RestAuthenticationEntryPoint entryPoint){
        this.passwordEncoder = encoder;
        this.userService = service;
        this.eventsService = eventsService;
        this.entryPoint = entryPoint;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(getDaoAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.httpBasic()
                .authenticationEntryPoint(entryPoint)
                .and()
                .csrf().disable().headers().frameOptions().disable()
                .and()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
                .and()
                .authorizeRequests()
                .mvcMatchers(HttpMethod.POST, "api/auth/signup").permitAll()
                .mvcMatchers( "api/acct/**").hasAuthority("ROLE_ACCOUNTANT")
                .mvcMatchers("api/admin/**").hasAuthority("ROLE_ADMINISTRATOR")
                .mvcMatchers(HttpMethod.GET, "api/empl/payment").hasAnyAuthority("ROLE_ACCOUNTANT", "ROLE_USER")
                .mvcMatchers("api/security/**").hasAuthority("ROLE_AUDITOR")
                .mvcMatchers(HttpMethod.POST, "api/auth/changepass").authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public DaoAuthenticationProvider getDaoAuthenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationProvider.setUserDetailsService(userService);
        return authenticationProvider;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new AccessDeniedHandlerExceptionHandler(userService, eventsService);
    }
}
