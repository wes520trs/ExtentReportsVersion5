import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyExtentReports {

    public WebDriver driver;
    public ExtentSparkReporter spark;
    public ExtentReports extent;
    public ExtentTest logger;

    @BeforeTest
    public void startReport() {
        // create an object of Extent Reports
        extent = new ExtentReports();

        spark = new ExtentSparkReporter(System.getProperty("user.dir") + "/test-output/extentSparkReport.html");
        extent.attachReporter(spark);
        extent.setSystemInfo("Host name", "Software Testing");
        extent.setSystemInfo("Invironment", "Production");
        extent.setSystemInfo("User name", "Tursun");

        spark.config().setDocumentTitle("Spark Report");
        spark.config().setReportName("Demo Spark Report");
        spark.config().setTheme(Theme.STANDARD);
    }

    // this method is to capture the screenshot and return the path of the screenshot in String type
    public static String getScreenShot(WebDriver driver, String screenShotName) throws IOException {
        String dateName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        TakesScreenshot ts = (TakesScreenshot) driver;
        File source = ts.getScreenshotAs(OutputType.FILE);
        String destination = System.getProperty("user.dir") + "/ScreenShots/" + screenShotName + dateName + ".jpeg";
        File finalDestination = new File(destination);
        FileUtils.copyFile(source, finalDestination);
        return destination;
    }

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://www.google.com");
    }

    @Test
    public void verifyTitle() {
        logger = extent.createTest("To verify Google title");
        System.out.println("Web page title is: " + driver.getTitle());
        Assert.assertEquals(driver.getTitle(), "Google");
    }

    @Test
    public void verifyLogo() {
        logger = extent.createTest("To verify Google Logo");
        boolean img = driver.findElement(By.xpath("//img[@alt=\"Google\"]")).isDisplayed();
        logger.createNode("Image is present");
        Assert.assertTrue(img);
        logger.createNode("Image is not present");
        Assert.assertFalse(img);
    }

    @AfterMethod
    public void getResult(ITestResult result) throws IOException {
        if (result.getStatus() == ITestResult.FAILURE) {
            // markupHelper is used to display the output in different colors
            logger.log(Status.FAIL, MarkupHelper.createLabel(result.getName() + " - Test Case Failed.", ExtentColor.RED));
            logger.log(Status.FAIL, MarkupHelper.createLabel(result.getThrowable() + " - Test case failed", ExtentColor.RED));
            // to capture screenshot path and store the path of the screenshot in the String "screenshotPath"
            String screenshotPath = getScreenShot(driver, result.getName());
            // to add it in the Extent report
            logger.fail("Test case failed snapshot is below: " + logger.addScreenCaptureFromPath(screenshotPath));
        } else if (result.getStatus() == ITestResult.SKIP) {
            logger.log(Status.SKIP, MarkupHelper.createLabel(result.getName() + " - Test Case Skipped", ExtentColor.ORANGE));
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            logger.log(Status.PASS, MarkupHelper.createLabel(result.getName() + " - Test Case Passed", ExtentColor.GREEN));
        }
        driver.quit();
    }

    @AfterTest
    public void endReport() {
        extent.flush();
    }
}
