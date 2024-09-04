import io.jboot.app.JbootApplication;
import io.jboot.codegen.CodeGenHelpler;
import io.jboot.codegen.model.JbootBaseModelGenerator;
import io.jboot.codegen.model.JbootModelGenerator;
import io.jboot.codegen.service.JbootServiceImplGenerator;
import io.jboot.codegen.service.JbootServiceInterfaceGenerator;

public class GenTester {

    public static void main(String[] args) {

        //配置数据量的连接信息，可以通过 JbootApplication.setBootArg 来配置
        //也可以在 jboot.properties 里配置
        JbootApplication.setBootArg("jboot.datasource.url", "jdbc:mysql://127.0.0.1:3306/db_shixun");
        JbootApplication.setBootArg("jboot.datasource.user", "root");
        JbootApplication.setBootArg("jboot.datasource.password", "1234");


        String modelPackage = "io.jboot.test.codegen.model"; //生成的Model的包名
        String baseModelPackage = "io.jboot.test.codegen.modelbase"; //生成的BaseModel的包名

        //Model存放的路径，一般情况下是 /src/main/java 下，如下是放在 test 目录下
        String modelDir = CodeGenHelpler.getUserDir() + "/src/test/java/" + modelPackage.replace(".", "/");
        String baseModelDir = CodeGenHelpler.getUserDir() + "/src/test/java/" + baseModelPackage.replace(".", "/");

        System.out.println("start generate...");
        System.out.println("generate dir:" + modelDir);

        //开始生成 Model 和 BaseModel 的代码
        new JbootBaseModelGenerator(baseModelPackage, baseModelDir).setGenerateRemarks(true).generate();
        new JbootModelGenerator(modelPackage, baseModelPackage, modelDir).generate();


        String servicePackage = "io.jboot.test.codegen.service"; // service 层的接口包名
        String serviceImplPackage = "io.jboot.test.codegen.service.provider"; // service 层的接口实现类包名


        //设置 service 层代码的存放目录
        String serviceOutputDir = CodeGenHelpler.getUserDir() + "/src/test/java/" + servicePackage.replace(".", "/");
        String serviceImplOutputDir = CodeGenHelpler.getUserDir() + "/src/test/java/" + serviceImplPackage.replace(".", "/");


        //开始生成代码
        new JbootServiceInterfaceGenerator(servicePackage, serviceOutputDir, modelPackage).generate();
        new JbootServiceImplGenerator(servicePackage, serviceImplPackage, serviceImplOutputDir, modelPackage).setImplName("provider").generate();

    }
}