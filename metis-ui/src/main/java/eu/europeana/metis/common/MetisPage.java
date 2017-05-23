package eu.europeana.metis.common;

import de.flapdoodle.embed.process.collections.Collections;
import eu.europeana.metis.mapping.organisms.pandora.UserProfile;
import eu.europeana.metis.page.PageView;
import eu.europeana.metis.templates.CssFile;
import eu.europeana.metis.templates.Footer;
import eu.europeana.metis.templates.FooterNavigation;
import eu.europeana.metis.templates.Global;
import eu.europeana.metis.templates.JsFile;
import eu.europeana.metis.templates.JsVar;
import eu.europeana.metis.templates.ListOfLinks;
import eu.europeana.metis.templates.Logo;
import eu.europeana.metis.templates.MenuItem;
import eu.europeana.metis.templates.MetisFooter;
import eu.europeana.metis.templates.MetisHeader;
import eu.europeana.metis.templates.Navigation;
import eu.europeana.metis.templates.NextPrev;
import eu.europeana.metis.templates.Options;
import eu.europeana.metis.templates.Social;
import eu.europeana.metis.templates.SubFooter;
import eu.europeana.metis.templates.Submenu;
import eu.europeana.metis.templates.SubmenuItem;
import eu.europeana.metis.templates.UtilityNav;
import eu.europeana.metis.templates.page.landingpage.Breadcrumb;
import eu.europeana.metis.templates.page.landingpage.I18n;
import eu.europeana.metis.templates.page.landingpage.I18nGlobal;
import eu.europeana.metis.templates.page.landingpage.Newsletter;
import java.util.Arrays;
import java.util.List;

/**
 * This is common Metis page with the same assets, bread-crumbs and header instantiated.
 *
 * @author alena
 */
public abstract class MetisPage extends AbstractMetisPage {
  public UserProfile user;

  @Override
  public List<CssFile> resolveCssFiles() {
    CssFile cssFile1 = new CssFile();
    CssFile cssFile2 = new CssFile();
    cssFile1.setPath("https://europeana-styleguide-test.s3.amazonaws.com/css/pandora/screen.css");
    cssFile1.setMedia("all");
    cssFile2.setPath("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.1.0/css/font-awesome.min.css");
    cssFile2.setMedia("all");

    return Arrays.asList(cssFile1, cssFile2);
  }

  @Override
  public List<JsFile> resolveJsFiles() {
    JsFile jsFile = new JsFile();
    jsFile.setPath("https://europeana-styleguide-test.s3.amazonaws.com/js/modules/require.js");
    jsFile.setDataMain(
        "https://europeana-styleguide-test.s3.amazonaws.com/js/modules/main/templates/main-pandora");

    return Arrays.asList(jsFile);
  }

  @Override
  public List<JsVar> resolveJsVars() {
    JsVar jsVar = new JsVar();
    jsVar.setName("pageName");
    jsVar.setValue("portal/index");
    return Arrays.asList(jsVar);
  }

  @Override
  public List<Breadcrumb> resolveBreadcrumbs() {
    Breadcrumb breadcrumb = new Breadcrumb();
    breadcrumb.setText("Home");
    breadcrumb.setUrl("/");
    return Arrays.asList(breadcrumb);
  }

  @Override
  public MetisHeader buildMetisHeader(PageView pageView) {

    Options options = new Options();
    options.setSearchActive(false);
    options.setSettingsActive(true);
    options.setOursitesHidden(true);
    Logo logo = new Logo();
    logo.setUrl("#");
    logo.setText("Europeana Pandora");

    MenuItem menuItem = new MenuItem();
    menuItem.setText("Sign In");
    menuItem.setUrl("#");
    menuItem.setFontawesome(true);
    menuItem.setIcon("users");
    menuItem.setIconClass("svg-icon-user-signin");
    menuItem.setSubmenu(buildNavigationSubmenu(pageView));

    UtilityNav utilityNav = new UtilityNav();
    utilityNav.setMenuId("settings-menu");
    utilityNav.setStyleModifier("caret-right");
    utilityNav.setTabindex("6");
    utilityNav.setItems(Collections.newArrayList(menuItem));

    Global global = new Global();
    global.setOptions(options);
    global.setLogo(logo);
    global.setUtilityNav(utilityNav);
    NextPrev nextPrev = new NextPrev();
    nextPrev.setPrevUrl("prev_url_here");
    nextPrev.setNextUrl("next_url_here");
    nextPrev.setResultsUrl("results_url_here");
    Navigation navigation = new Navigation();
    navigation.setHomeUrl("#");
    navigation.setHomeUrl("Home");
    navigation.setNextPrev(nextPrev);
    navigation.setFooter(true);
    navigation.setGlobal(global);
    MetisHeader metisHeader = new MetisHeader();
    metisHeader.setNavigation(navigation);

    return metisHeader;
  }

  private Submenu buildNavigationSubmenu(PageView pageView) {
    SubmenuItem submenuItem1 = new SubmenuItem();
    SubmenuItem submenuItem2 = new SubmenuItem();
    submenuItem1.setIsCurrent(true);
    submenuItem1.setIsDivider(null);
    submenuItem1.setSubtitle(null);
    submenuItem1.setMessage(null);
    submenuItem1.setSubmenu(false);
    submenuItem2.setIsCurrent(false);
    submenuItem2.setIsDivider(null);
    submenuItem2.setSubtitle(null);
    submenuItem2.setMessage(null);
    submenuItem2.setSubmenu(false);
    switch (pageView) {
      case REGISTER:
        submenuItem1.setText("Login");
        submenuItem1.setUrl("/login");
        submenuItem2.setText("Home");
        submenuItem2.setUrl("/");
        break;
      case LOGIN:
        submenuItem1.setText("Register");
        submenuItem1.setUrl("/register");
        submenuItem2.setText("Home");
        submenuItem2.setUrl("/");
        break;
      default:
        if (user != null && user.getEmail() != null) {
          submenuItem1.setText("Profile");
          submenuItem1.setUrl("/profile");
          submenuItem2.setText("Logout");
          submenuItem2.setUrl("/logout");
        } else {
          submenuItem1.setText("Login");
          submenuItem1.setUrl("/login");
          submenuItem2.setText("Register");
          submenuItem2.setUrl("/register");
        }
        break;
    }
    Submenu submenu = new Submenu();
    submenu.setItems(Collections.newArrayList(submenuItem1, submenuItem2));
    return submenu;
  }

  @Override
  public MetisFooter buildMetisFooter() {

    SubmenuItem submenuItem1 = new SubmenuItem();
    SubmenuItem submenuItem2 = new SubmenuItem();
    SubmenuItem submenuItem3 = new SubmenuItem();
    SubmenuItem submenuItem4 = new SubmenuItem();
    submenuItem1.setText("Home");
    submenuItem1.setUrl("/");
    submenuItem2.setText("Terms of use and policies");
    submenuItem2.setUrl("http://europeana.eu/portal/rights/terms-and-policies.html");
    submenuItem3.setText("Contact us");
    submenuItem3.setUrl("/");
    submenuItem4.setText("Sitemap");
    submenuItem4.setUrl("/");

    SubFooter subFooter = new SubFooter();
    subFooter.setItems(Collections.newArrayList(submenuItem1, submenuItem2, submenuItem3, submenuItem4));
    Social social = new Social();
    social.setFacebook(true);
    social.setGithub(false);
    social.setGoogleplus(true);
    social.setLinkedin(false);
    social.setPinterest(true);
    social.setTwitter(true);

    ListOfLinks listOfLinks1 = new ListOfLinks();
    SubmenuItem listOflinks1SubmenuItem1 = new SubmenuItem();
    SubmenuItem listOflinks1SubmenuItem2 = new SubmenuItem();
    SubmenuItem listOflinks1SubmenuItem3 = new SubmenuItem();
    SubmenuItem listOflinks1SubmenuItem4 = new SubmenuItem();
    SubmenuItem listOflinks1SubmenuItem5 = new SubmenuItem();
    listOflinks1SubmenuItem1.setText("About");
    listOflinks1SubmenuItem1.setUrl("/");
    listOflinks1SubmenuItem2.setText("Development updates");
    listOflinks1SubmenuItem2.setUrl("/");
    listOflinks1SubmenuItem3.setText("All institutions");
    listOflinks1SubmenuItem3.setUrl("/");
    listOflinks1SubmenuItem4.setText("Become our partner");
    listOflinks1SubmenuItem4.setUrl("/");
    listOflinks1SubmenuItem5.setText("Contact us");
    listOflinks1SubmenuItem5.setUrl("/");
    listOfLinks1.setTitle("More Info");
    listOfLinks1.setItems(Collections.newArrayList(listOflinks1SubmenuItem1, listOflinks1SubmenuItem2, listOflinks1SubmenuItem3, listOflinks1SubmenuItem4, listOflinks1SubmenuItem5));
    ListOfLinks listOfLinks2 = new ListOfLinks();
    SubmenuItem listOflinks2SubmenuItem1 = new SubmenuItem();
    SubmenuItem listOflinks2SubmenuItem2 = new SubmenuItem();
    listOflinks2SubmenuItem1.setText("Search tips");
    listOflinks2SubmenuItem1.setUrl("/");
    listOflinks2SubmenuItem2.setText("Terms of Use & Policies");
    listOflinks2SubmenuItem2.setUrl("/");
    listOfLinks2.setTitle("Help");
    listOfLinks2.setItems(Collections.newArrayList(listOflinks2SubmenuItem1, listOflinks2SubmenuItem2));
    ListOfLinks listOfLinks3 = new ListOfLinks();
    SubmenuItem listOflinks3SubmenuItem1 = new SubmenuItem();
    SubmenuItem listOflinks3SubmenuItem2 = new SubmenuItem();
    listOflinks3SubmenuItem1.setText("API Docs");
    listOflinks3SubmenuItem1.setUrl("/");
    listOflinks3SubmenuItem2.setText("Status");
    listOflinks3SubmenuItem2.setUrl("/");
    listOfLinks3.setTitle("Tools");
    listOfLinks3.setItems(Collections.newArrayList(listOflinks3SubmenuItem1, listOflinks3SubmenuItem2));


    Footer footer = new Footer();
    footer.setSubfooter(subFooter);
    footer.setLinklist1(listOfLinks1);
    footer.setLinklist2(listOfLinks2);
    footer.setLinklist3(listOfLinks3);
    footer.setSocial(social);
    FooterNavigation footerNavigation = new FooterNavigation();
    footerNavigation.setFooter(footer);
    MetisFooter metisFooter = new MetisFooter();
    metisFooter.setNavigation(footerNavigation);

    return metisFooter;
  }

  public I18n buildI18n() {
    Newsletter newsletter = new Newsletter();
    newsletter.setChooseLanguage("Choose a language");
    newsletter.setEmailAddressInvalid("Please enter a valid email address.");
    newsletter.setEmailAddressRequired("Please enter your email address.");
    newsletter.setLanguageRequired("Please choose a language for your newsletter.");
    newsletter.setSignup("Sign up for our newsletter");
    newsletter.setSubmitAlt("Subscribe");
    I18nGlobal i18nGlobal = new I18nGlobal();
    i18nGlobal.setFindUsElsewhere("Find us elsewhere");
    i18nGlobal.setMission("We transform the world with culture! We want to build on Europe’s rich heritage and make it easier for people to use, whether for work, for learning or just for fun.");
    i18nGlobal.setMissionTitle("Our mission");
    i18nGlobal.setNewsletter(newsletter);
    I18n i18n = new I18n();
    i18n.setGlobal(i18nGlobal);

    return i18n;
  }
}