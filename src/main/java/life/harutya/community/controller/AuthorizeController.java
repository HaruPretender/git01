package life.harutya.community.controller;

import life.harutya.community.dto.AccessTokenDTO;
import life.harutya.community.dto.GitHubUser;
import life.harutya.community.mapper.UserMapper;
import life.harutya.community.model.User;
import life.harutya.community.provider.GitHubProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class AuthorizeController {

    @Autowired
    private GitHubProvider gitHubProvider;

    @Autowired
    private UserMapper userMapper;

    @Value("${github.client.id}")
    private String clientId;
    @Value("${github.client.secret}")
    private String clientSecret;
    @Value("${github.redirect.uri}")
    private String clientUrl;


    @GetMapping("/callback")
    public String callback(@RequestParam(name="code") String code,
                           @RequestParam(name="state") String state,
                            HttpServletResponse response){
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setCode(code);
        accessTokenDTO.setRedirect_uri(clientUrl);
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setState(state);
        String accessToken = gitHubProvider.getAccessTokenDTO(accessTokenDTO);
        GitHubUser gitHubUser = gitHubProvider.getUser(accessToken);
        if(gitHubUser != null){
            User user = new User(); //获取用户信息
            String token = UUID.randomUUID().toString(); //生成一个token
            //设置用户信息
            user.setToken(token);
            user.setName(gitHubUser.getName());
            user.setAccount_Id(String.valueOf(gitHubUser.getId()));
            user.setGmtCreate(System.currentTimeMillis());
            user.setGmtModified(user.getGmtCreate());
            //将用户信息插入到数据库
            userMapper.insert(user);
            response.addCookie(new Cookie("token",token));
            return "redirect:/";
        }
        else{
            //登录失败
            return "redirect:/";
        }
    }
}
