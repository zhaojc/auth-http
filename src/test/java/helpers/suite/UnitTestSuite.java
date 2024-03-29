package helpers.suite;

import helpers.category.ServletContainer;
import helpers.category.UnitTests;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.rootservices.otter.router.GetServletURIImplTest;
import org.rootservices.otter.QueryStringToMapImplTest;
import org.rootservices.otter.authentication.ParseHttpBasicImplTest;

/**
 * Created by tommackenzie on 4/23/15.
 */
@RunWith(Categories.class)
@Categories.IncludeCategory(UnitTests.class)
@Categories.ExcludeCategory(ServletContainer.class)
@Suite.SuiteClasses({
        QueryStringToMapImplTest.class,
        GetServletURIImplTest.class,
        ParseHttpBasicImplTest.class
})
public class UnitTestSuite {
}
