package com.tnsoft.web.security;

import com.tnsoft.hibernate.model.Permission;
import com.tnsoft.hibernate.model.User;
import com.tnsoft.web.dao.PermissionDAO;
import com.tnsoft.web.dao.UserDAO;
import com.tnsoft.web.model.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author z
 */
@Service
public class LoginUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PermissionDAO permissionDAO;

    public LoginUserDetailsService() {
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userDAO.getUserByName(username);
        if (user != null) {
            List<Permission> permissions = permissionDAO.findByUserId(user.getId());
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            for (Permission permission : permissions) {
                if (null != permission && null != permission.getName()) {
                    GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(permission.getName());
                    grantedAuthorities.add(grantedAuthority);
                }
            }
            if (user.getStatus() == Constants.UserState.STATE_CANCLE) {
                throw new DisabledException(username + "，该账号被禁用！");
            }
            boolean enabled = true;
            boolean accountNonExpired = true;
            boolean credentialsNonExpired = true;
            boolean accountNonLocked = true;
            return new LoginUser(user.getId(),
                    user.getNickName(),
                    user.getStatus(),
                    user.getName(),
                    user.getPassword(),
                    user.getDomainId(),
                    user.getRootDomainId(),
                    enabled,
                    accountNonExpired,
                    credentialsNonExpired,
                    accountNonLocked,
                    grantedAuthorities);
        } else {
            throw new UsernameNotFoundException(username + "，该用户名不存在！");
        }
    }
}
