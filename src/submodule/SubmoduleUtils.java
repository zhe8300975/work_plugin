package submodule;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubmoduleUtils {

    private static final String temp = "[submodule \"%s\"]\n" +
            "\tpath = %s\n" +
            "\turl = %s\n" +
            "\tbranch = %s\n";

    public static void writeToFile(String content, String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outputStream.close();
            inputStream.close();
        }

        return outputStream.toByteArray();
    }


    public static String readFile(String filePath) {

        File file = new File(filePath);
        InputStream in = null;
        String content = "";

        if (file.isFile() && file.exists()) {
            //判断文件是否存在
            try {
                in = new FileInputStream(file);
                content = new String(readStream(in));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return content;
    }


    public static LinkedHashMap<String, SubModuleParse> parseModules(String modules) {
        LinkedHashMap hashMap = new LinkedHashMap();
        if (!StringUtils.isEmpty(modules)) {
            Pattern pattern = Pattern.compile("\\[submodule \\\"[\\S]*\\\"\\][^\\[\\]]*");
            Matcher matcher = pattern.matcher(modules);
            while (matcher.find()) {
                SubModuleParse subModuleParse = parseModule(matcher.group());
                if (subModuleParse.isAvailable()) {
                    hashMap.put(subModuleParse.path, subModuleParse);
                }
            }
        }
        return hashMap;
    }

    public static void writeModules(HashMap<String, SubModuleParse> hashMap, String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, SubModuleParse> item : hashMap.entrySet()) {
            stringBuilder.append(String.format(temp, item.getValue().module, item.getValue().path, item.getValue().url, item.getValue().branch));
        }
        writeToFile(stringBuilder.toString(), filePath);
    }

    private static SubModuleParse parseModule(String module) {
        String lines[] = module.split("\n");
        SubModuleParse subModuleParse = new SubModuleParse();
        String pattern = "\\[submodule \\\"(\\S*)\\\"\\]";
        Pattern r = Pattern.compile(pattern);
        for (String line : lines) {
            // 现在创建 matcher 对象
            if (line.contains("=")) {
                String params[] = line.split("=");
                switch (params[0].trim()) {
                    case "path":
                        subModuleParse.path = params[1].trim();
                        break;
                    case "url":
                        subModuleParse.url = params[1].trim();
                        break;
                    case "branch":
                        subModuleParse.branch = params[1].trim();
                        break;
                }
            } else if (line.contains("submodule")) {
                Matcher m = r.matcher(line);
                if (m.find()) {
                    subModuleParse.module = m.group(1).trim();
                }
            }
        }
        return subModuleParse;
    }

    public static void main(String[] args) {
//        String content = "[submodule \"common\"]\n" +
//                "\tpath = common\n" +
//                "\turl = git@gitlab.p1staff.com:android/libs/android-common.git\n" +
//                "  branch = master\n" +
//                "[submodule \"putong-common\"]\n" +
//                "\tpath = putong-common\n" +
//                "\turl = git@gitlab.p1staff.com:haoyonglong/putong-common.git\n" +
//                "  branch = feature_quick_chat_funnel_optimize\n" +
//                "[submodule \"business/b_feed\"]\n" +
//                "\tpath = business/b_feed\n" +
//                "\turl = git@gitlab.p1staff.com:android/modules/b_feed.git\n" +
//                "  branch = master\n" +
//                "[submodule \"business/b_account\"]\n" +
//                "\tpath = business/b_account\n" +
//                "\turl = git@gitlab.p1staff.com:android/modules/b_account.git\n" +
//                "  branch = master\n" +
//                "[submodule \"business/b_live\"]\n" +
//                "\tpath = business/b_live\n" +
//                "\turl = git@gitlab.p1staff.com:android/modules/b_live.git\n" +
//                "  branch = master\n" +
//                "[submodule \"business/b_core\"]\n" +
//                "\tpath = business/b_core\n" +
//                "\turl = git@gitlab.p1staff.com:haoyonglong/b_core.git\n" +
//                "  branch = feature_quick_chat_funnel_optimize\n" +
//                "[submodule \"putong-data\"]\n" +
//                "\tpath = putong-data\n" +
//                "\turl = git@gitlab.p1staff.com:haoyonglong/putong-data.git\n" +
//                "  branch = feature_quick_chat_funnel_optimize\n";
//        parseModules(content);
        String command = "git commit -m \"'gitmodule 等修改\"'";
//        String[] cmds = { "/bin/sh", "-c", command };
        String[] envp = {"LANG=UTF-8"};
        try {
            callShell(command, "Users/zhaowencong/Tantan/plugin_putong/putong-android/business/b_core/");
        } catch (Throwable e) {
            if (e instanceof ShellThrow) {
                System.out.println(((ShellThrow) e).throwContent);
            } else {
                e.printStackTrace();
            }
        }
    }


    public SubmoduleUtils() {
    }


    public static String callShell(String shellString, String path) throws ShellThrow {
        return callShell(shellString, path, false);
    }

    public static String callShell(String shellString, String path, boolean mayTimeOut) throws ShellThrow {
        return callShell(shellString, path, mayTimeOut, false);
    }

    public static String callShell(String shellString, String path, boolean mayTimeOut, boolean isFile) throws ShellThrow {
        return callShell(shellString, path, null, mayTimeOut, isFile);
    }

    public static String callShell(String shellString, String path, String[] envp, boolean mayTimeOut, boolean isFile) throws ShellThrow {
        BufferedReader bufrIn = null;
        BufferedReader bufrError = null;
        StringBuilder result = new StringBuilder();
        Process process = null;
        int exitValue;
        try {
            if (isFile) {
                process = callShellFile(shellString, path, envp);
            } else {
                process = Runtime.getRuntime().exec(shellString, envp, new File(path));
            }
            if (mayTimeOut) {
                ShellWorker worker = new ShellWorker(process);
                worker.start();
                worker.join(10000);
                if (worker.exit != null) {
                    exitValue = worker.exit;
                } else {
                    throw new ShellThrow("error Timeout: " + shellString + "\n请确保网络后重试");
                }
            } else {
                exitValue = process.waitFor();
            }
            // 获取命令执行结果, 有两个结果: 正常的输出 和 错误的输出（PS: 子进程的输出就是主进程的输入）
            bufrIn = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            bufrError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
            result.append("执行：" + shellString + "\n");
            String line = null;
            while ((line = bufrIn.readLine()) != null) {
                result.append(line).append('\n');
            }
            while ((line = bufrError.readLine()) != null) {
                result.append(line).append('\n');
            }
            if (0 != exitValue) {
                throw new ShellThrow("error : " + result.toString() + "call shell failed. error code is :" + exitValue);
            }
        } catch (Throwable e) {
            if (e instanceof ShellThrow) {
                throw ((ShellThrow) e);
            }
            closeStream(bufrIn);
            closeStream(bufrError);
            // 销毁子进程
            if (process != null) {
                process.destroy();
            }
            throw new ShellThrow("error : " + result.toString() + " call shell failed. " + e);
        }
        return result.toString();
    }

    public static Process callShellFile(String shellString, String path, String[] envp) throws IOException {
        Process process = Runtime.getRuntime().exec("/bin/bash", envp, new File("/bin"));
        PrintWriter out = null;
        try {
            if (process != null) {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);
                out.println("cd " + path); //执行该语句后返回上一级目录
                out.println("pwd");//打印当前目录
                out.println(shellString);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return process;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    private static class ShellWorker extends Thread {
        private final Process process;
        private Integer exit;

        private ShellWorker(Process process) {
            this.process = process;
        }

        public void run() {
            try {
                exit = process.waitFor();
            } catch (InterruptedException ignore) {
                return;
            }
        }
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                // nothing
            }
        }
    }

    public static class ShellThrow extends Throwable {
        String throwContent;

        ShellThrow(String throwContent) {
            this.throwContent = throwContent;
        }

    }



}


