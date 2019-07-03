/**
 * Copyright Kirill Semenov
 * 2019
 */
package hotswap.controller;

import net.openhft.compiler.CompilerUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import hotswap.test.TestClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class HotSwapController {

    private static Pattern packageRegexp = Pattern.compile("^package\\s+(?<package>(\\w|\\.)+);");
    private static Pattern classNameRegexp = Pattern.compile("class\\s+(?<className>(\\w|\\d)+)\\{?");

    @GetMapping("/")
    public String initScreen(Model model) {
        ClassWrapper empty = new ClassWrapper();
        empty.setBody("Insert your class of your code here");
        model.addAttribute("clazz", empty);
        return "hotswapscreen";
    }

    @PostMapping("/update-class")
    public String updateClass(@ModelAttribute("clazz") ClassWrapper clazz, Model model) throws ClassNotFoundException {
        try {
            String wholeClazz = clazz.getBody();
            Matcher packageNameMatcher = packageRegexp.matcher(wholeClazz);
            if (!packageNameMatcher.find()) {
                throw new IllegalArgumentException("Couldn't find package");
            }
            Matcher classNameMatcher = classNameRegexp.matcher(wholeClazz);
            if (!classNameMatcher.find()) {
                throw new IllegalArgumentException("Couldn't find class name");
            }
            String packageName = packageNameMatcher.group("package");
            String className = classNameMatcher.group("className");

            Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(
                    packageName + "." + className,
                    wholeClazz
            );
        } catch (Exception e) {
            model.addAttribute("result", "Failed! <br> " + e.getMessage());
            return "hotswapscreen";
        }
        model.addAttribute("result", "Loaded successfully!");
        return "hotswapscreen";
    }

    @GetMapping("/test")
    public String test(Model model) {
        String testString = new TestClass().getText();
        model.addAttribute("test_text", testString);
        return "test";
    }

}
