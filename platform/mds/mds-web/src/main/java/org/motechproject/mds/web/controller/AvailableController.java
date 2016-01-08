package org.motechproject.mds.web.controller;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.motechproject.mds.domain.Relationship;
import org.motechproject.mds.dto.TypeDto;
import org.motechproject.mds.service.TypeService;
import org.motechproject.mds.web.SelectData;
import org.motechproject.mds.web.SelectResult;
import org.motechproject.mds.web.comparator.TypeDisplayNameComparator;
import org.motechproject.mds.web.matcher.TypeMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.motechproject.mds.util.Constants.Roles;

/**
 * The <code>AvailableController</code> is the Spring Framework Controller, used by view layer for
 * retrieving available objects, for example field types.
 *
 * @see org.motechproject.mds.dto.TypeDto
 */
@Controller
public class AvailableController extends MdsController {
    private MessageSource messageSource;
    private TypeService typeService;

    @RequestMapping(value = "/available/types", method = RequestMethod.GET)
    @ResponseBody
    public SelectResult<TypeDto> getTypes(SelectData data) {
        List<TypeDto> list = typeService.getAllTypes();

        // The Long and Date types are available for DDEs exclusively
        list.remove(typeService.findType(Long.class));
        list.remove(typeService.findType(Date.class));
        list.remove(typeService.findType(Relationship.class));

        //The DateTime and LocalDate types from Joda are available for DDEs exclusively
        list.remove(typeService.findType(DateTime.class));
        list.remove(typeService.findType(org.joda.time.LocalDate.class));

        // TextArea type is available only from UI
        list.add(textAreaUIType());

        CollectionUtils.filter(list, new TypeMatcher(data.getTerm(), messageSource));
        Collections.sort(list, new TypeDisplayNameComparator(messageSource));

        return new SelectResult<>(data, list);
    }

    @RequestMapping(value = "/available/mdsTabs", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getTabs() {
        List<String> availableTabs = new ArrayList<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(Roles.DATA_ACCESS))) {
            availableTabs.add("dataBrowser");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SCHEMA_ACCESS))) {
            availableTabs.add("schemaEditor");
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority(Roles.SETTINGS_ACCESS))) {
            availableTabs.add("settings");
        }

        return availableTabs;
    }

    @Resource(name = "mdsMessageSource")
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Autowired
    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }
}
