package com.tnsoft.web.controller;

import com.expertise.common.util.StringUtils;
import com.tnsoft.hibernate.BaseHibernateUtils;
import com.tnsoft.hibernate.DbSession;
import com.tnsoft.hibernate.model.Domain;
import com.tnsoft.web.model.Constants;
import com.tnsoft.web.service.DomainService;
import com.tnsoft.web.util.Utils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.*;

@Controller
public class DomainController extends BaseController {

    @Resource(name = "domainService")
    private DomainService domainService;

    public DomainController() {
        super();
    }


    @RequestMapping("/domain")
    public String domain(Model model) {
        Utils.saveLog(lg.getUserId(), "查看站点的列表");

        if (!validateUser()) {
            return "redirect:/";
        }

        model.addAttribute("username", lg.getUserName());
        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
        model.addAttribute("domainId", lg.getDomainId());

        return "view.domain.domain";
    }

//    @RequestMapping("/editDomain")
//    public ModelAndView editDomain(Model model, String id) {
//        if (!validateUser()) {
//            return new ModelAndView("redirect:/");
//        }
//        Domain domain = null;
//        if (!StringUtils.isEmpty(id) && Integer.parseInt(id) > 0) {
//            // 修改
//            model.addAttribute("id", id);
//            domain = domainService.getById(Integer.parseInt(id));
////			DbSession session = BaseHibernateUtils.newSession();
////			try {
////				domain = (Domain) session.get(Domain.class, Integer.parseInt(id));
////				System.out.println("-------------"+domain.getName());
////			} finally {
////				session.close();
////			}
//        } else {
//            // 新增
//            domain = new Domain();
//            List<SelectItem> items = new ArrayList<SelectItem>();
//            // domainPath和roleId
//            if (lg.getDomain().getDomainPath().equals("/")) {
//                // 超级管理员获得所有站点除了/
//                SelectItem item = new SelectItem("0", "请选择");
//                items.add(item);
//                List<Domain> domains = domainService.getAll();
//                SelectItem item2;
//                for (Domain nd : domains) {
//                    item2 = new SelectItem(nd.getId() + "", nd.getName());
//                    items.add(item2);
//                }
//                model.addAttribute("domains", items);
//            } else {
//                // 管理员新增站点就是自己旗下的不用选择
//                SelectItem item = new SelectItem(lg.getDomain().getId() + "", lg.getDomain().getName());
//                items.add(item);
//                model.addAttribute("domains", items);
//            }
//        }
//
//        model.addAttribute("username", lg.getUserName());
//        model.addAttribute("rolename", lg.getDefRole().getName() + lg.getNickName());
//
//        return new ModelAndView("view.domain.editDomain", "command", domain);
//    }


//    @RequestMapping("/saveDomain")
//    public String saveDomain(Model model, String name, String id, String phone, String address, String description,
//                             RedirectAttributes attr) {
//
//        if (!validateUser()) {
//            return "view.login";
//        }
//
//        DbSession db = BaseHibernateUtils.newSession();
//        try {
//            db.beginTransaction();
//
//            Date now = new Date();
//            if (!StringUtils.isEmpty(id) && Integer.parseInt(id) > 0) {
//                Domain domain = domainService.getById(id);
//                if (domain != null) {
//                    domain.setName(name);
//                    domain.setPhone(phone);
//                    domain.setAddress(address);
//                    domain.setDescription(description);
//                    domain.setLastModified(now);
//                }
//                db.update(domain);
//                db.flush();
//
//                attr.addFlashAttribute("error", true);
//                attr.addFlashAttribute("message", "站点编辑成功");
//                Utils.saveLog(lg.getUserId(), "编辑站点", lg.getDomainId());
//
//            } else {
//                Domain domain = new Domain();
//                domain.setCreationTime(now);
//                domain.setName(name);
//                domain.setPhone(phone);
//                domain.setAddress(address);
//                domain.setDomainPath(lg.getDomain().getDomainPath() + lg.getDomainId() + "/");
//                domain.setDescription(description);
//                domain.setLastModified(now);
//                domain.setStatus(Constants.DomainState.STATE_ACTIVE);
//                db.save(domain);
//                db.flush();
//
//                User user = new User();
//                user.setName(phone);
//                user.setNickName(name);
//                user.setMobile(phone);
//                try {
//                    if (phone.length() > 6) {
//                        user.setPassword(
//                                AuthUtils.hash(phone, AuthUtils.newPassword(phone.substring(phone.length() - 6))));
//                    } else {
//                        user.setPassword(AuthUtils.hash(phone, AuthUtils.newPassword("123456")));
//                    }
//                } catch (GeneralSecurityException e) {
//                    Logger.error(e);
//                }
//                user.setCreationTime(now);
//                user.setLastModified(now);
//                user.setGender("男");
//                user.setType(Constants.Role.ADMIN);
//                user.setStatus(Constants.State.STATE_ACTIVE);
//                user.setDomainId(domain.getId());
//                db.save(user);
//                db.flush();
//
//                UserRole ur = new UserRole();
//                // ur.setFlag(1);
//                ur.setUserId(user.getId());
//                ur.setStatus(Constants.State.STATE_ACTIVE);
//                ur.setRoleId(Constants.Role.ADMIN);
//                db.save(ur);
//                db.flush();
//
//                // 报警设置
//                AlertLevel l = new AlertLevel();
//                l.setCreationTime(now);
//                l.setDomainId(domain.getId());
//                l.setHours(1);
//                l.setLastModified(now);
//                l.setTimes(1);
//                l.setName("紧急");
//                l.setStatus(Constants.State.STATE_ACTIVE);
//                db.save(l);
//
//                AlertLevel l2 = new AlertLevel();
//                l2.setCreationTime(now);
//                l2.setDomainId(domain.getId());
//                l2.setHours(1);
//                l2.setLastModified(now);
//                l2.setTimes(0);
//                l2.setName("模块失联");
//                l2.setStatus(Constants.State.STATE_ACTIVE);
//                db.save(l2);
//
//            }
//
//            attr.addFlashAttribute("error", true);
//            attr.addFlashAttribute("message", "站点新增成功");
//            Utils.saveLog(lg.getUserId(), "新增站点", lg.getDomainId());
//
//            db.commit();
//        } finally {
//            db.close();
//        }
//        return "redirect:/domain";
//    }

    @RequestMapping("/deleteDomain")
    public String deleteDomain(Model model, String id, int mode, RedirectAttributes attr) {

        if (!validateUser()) {
            return "view.login";
        }

        DbSession db = BaseHibernateUtils.newSession();
        try {
            db.beginTransaction();

            Date now = new Date();
            if (!StringUtils.isEmpty(id)) {
                Domain domain = (Domain) db.get(Domain.class, Integer.parseInt(id));
                if (domain != null) {
                    if (mode == 1) {
                        domain.setLastModified(now);
                        attr.addFlashAttribute("message", "站点停用成功");
                        Utils.saveLog(lg.getUserId(), "停用站点");

                        domain.setStatus(Constants.DomainState.STATE_DISABLE);
                    } else {
                        domain.setLastModified(now);
                        attr.addFlashAttribute("message", "站点恢复成功");
                        Utils.saveLog(lg.getUserId(), "恢复站点");

                        domain.setStatus(Constants.DomainState.STATE_ACTIVE);
                    }
                }
            }

            db.commit();

        } finally {
            db.close();
        }

        attr.addFlashAttribute("error", true);

        return "redirect:/domain";
    }

    @RequestMapping("/ajaxDomain")
    @ResponseBody
    public Object ajaxDomain(int draw, int start, int length) {
        if (!validateUser()) {
            return "";
        }

        DbSession session = BaseHibernateUtils.newSession();
        try {
            Map<String, Object> result = query(session, draw, start, length, " order by id ASC ");
            return result;
        } finally {
            session.close();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> query(DbSession db, int draw, int start, int length, String defaultOrderBy) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, String[]> properties = request.getParameterMap();

        String orderSql = "";

        String search = properties.get("search[value]")[0];

        long recordsFiltered = 0;
        long recordsTotal = count(db, null);
        result.put("recordsTotal", recordsTotal);

        String whereClause = "";

        if (!StringUtils.isEmpty(search)) {

            whereClause = " (a.name LIKE '%" + search + "%') ";
        }

        String status = properties.get("columns[4][search][value]")[0];
        if (!StringUtils.isEmpty(status)) {
            if (!StringUtils.isEmpty(whereClause)) {
                whereClause += " AND ";
            }
            whereClause += " a.status=" + status + " ";
        }
        if (!StringUtils.isEmpty(search) || !StringUtils.isEmpty(status)) {
            recordsFiltered = count(db, whereClause);
        } else {
            recordsFiltered = recordsTotal;
        }
        result.put("recordsFiltered", recordsFiltered);

        result.put("data", query(db, whereClause, orderSql, start, length));
        return result;

    }

    private int count(DbSession session, String where) {

        String sql = "from Domain a ";

        if (!StringUtils.isEmpty(where)) {
            sql += ("WHERE " + where);
        }
        Integer count = domainService.count(sql);
        return count == null ? 0 : count.intValue();
    }

    private List<Domain> query(DbSession session, String where, String order, int offset, int limit) {

        String sql = "from Domain a ";
        if (!StringUtils.isEmpty(where)) {
            sql += (" WHERE " + where);
        }

        if (!StringUtils.isEmpty(order)) {
            sql += (" " + order);
        }

        List<Domain> list = domainService.getByHQLWithLimits(offset, limit, sql);

        if (!list.isEmpty()) {
            for (Domain temp : list) {
                if (temp.getStatus() == Constants.DomainState.STATE_ACTIVE) {
                    temp.setStatusName("正常");
                } else {
                    temp.setStatusName("停用");
                }
            }
            return list;
        }

        return Collections.emptyList();
    }

}
