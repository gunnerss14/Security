package com.cos.security.config;

import com.cos.security.config.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity  // 스프링 시큐리티 필터(=SecurityConfig) 필터체인에 등록이 됨. @Configuration 기능을 포함하고 있음.
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
// secured 어노테이션 활성화, preAuthorize, postAuthorized 어노테이션 활성화
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    @Bean       // 해당 매서드의 리턴되는 오브젝트를 IoC로 등록해줌
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 구글로그인 완료된 뒤 후처리 필요
        // 1.코드받기(인증) 2.엑세스토큰(권한) 3.사용자프로필 정보 가져오기
        // 4-1. 정보토대로 회원가입 자동 진행시키기도 함.
        // 4-2. (이메일,전화번호,이름,아이디)쇼핑몰 > (집주소)백회점몰 > (vip등급, 일반등급)
        http.csrf().disable();  // REST API 방식을 사용할 때는 쿠키를 사용해서 인증하는 방식을 잘 사용하지 않기에 CSRF 토큰방식 설정을 꺼놔도 상관없음.
        http.authorizeRequests()
                .antMatchers("/user/**").authenticated()      // 인증만 되면 들어갈 수 있는 주소
                .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/loginForm")
                .loginProcessingUrl("/login")   // login 주소가 호출이 되면 시큐리티가 낚아채섯 대신 로그인을 진행시켜줌
                .defaultSuccessUrl("/")
                .and()
                .oauth2Login()
                .loginPage("/loginForm")
                .userInfoEndpoint()
                .userService(principalOauth2UserService); // 구글 로그인 완료된 뒤 후처리 필요. 코드x (엑세스토큰+사용자프로필정보 O)
    }
}
