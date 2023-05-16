package threadLocal;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
// 工厂模式
class InstanceFactory{
    public static DefaultThreadLocal CreateInstance(String type){
        DefaultThreadLocal defaultThreadLocal = null;
        if(type.equals("noThreadLocal")){
            defaultThreadLocal = TestNoThreadLocal.getInstance();
        }else if(type.equals("threadLocal")){
            defaultThreadLocal = TestWithThreadLocal.getInstance();
        }else if(type.equals("threadLocalStatic")){
            defaultThreadLocal = TestWithThreadLocalStatic.getInstance();
        }
        return  defaultThreadLocal;
    }
}

abstract class  DefaultThreadLocal{
    private String str;
    private  ThreadLocal<String> threadLocal = new ThreadLocal<String>();
    private  static ThreadLocal<String> threadLocalStatic = new ThreadLocal<String>();
    public abstract void show() throws InterruptedException;
    public void setStr(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public void setThreadLocal(ThreadLocal<String> threadLocal) {
        this.threadLocal = threadLocal;
    }

    public ThreadLocal<String> getThreadLocal() {
        return threadLocal;
    }

    public static void setThreadLocalStatic(ThreadLocal<String> threadLocalStatic) {
        DefaultThreadLocal.threadLocalStatic = threadLocalStatic;
    }

    public static ThreadLocal<String> getThreadLocalStatic() {
        return threadLocalStatic;
    }
}

// ************ 测试1 使用static ThreadLocal ***********************
// 使用ThreadLocal都导致线程1取到了 线程2设置的值2； 测试成功。
class ThreadLocalRunStatic {
    public static void main(String[] args) throws Exception {
        // 线程1
        FutureTask<ThreadLocal> futureTask = new FutureTask<ThreadLocal>(new MyCallableThreadLocalStatic());
        Thread thread01 = new Thread(futureTask);
        thread01.setName("thread01");
        thread01.start();
        // 线程2
        FutureTask<ThreadLocal> futureTask2 = new FutureTask<ThreadLocal>(new MyCallable2ThreadLocalStatic());
        Thread thread02 = new Thread(futureTask2);
        thread02.setName("thread02");
        thread02.start();
        try {
            ThreadLocal ins1 = futureTask.get();
            System.out.println(ins1);
            ThreadLocal ins2 = futureTask2.get();
            System.out.println(ins2);
            System.out.println(ins1 == ins2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}



// 线程2任务，把str值设置成2；实际场景中这个两个任务其实是同一方法，值来自不同客户端的入参。
class MyCallable2ThreadLocalStatic implements Callable<ThreadLocal>{
    DefaultThreadLocal instance = InstanceFactory.CreateInstance("threadLocalStatic");
    public ThreadLocal call() throws Exception {
        TestWithThreadLocalStatic.getThreadLocalStatic().set("2");
        instance.show();
        return TestWithThreadLocalStatic.getThreadLocalStatic();
    }
}

// 线程1任务，把str值设置成1；
class MyCallableThreadLocalStatic implements Callable<ThreadLocal>{
    DefaultThreadLocal instance = InstanceFactory.CreateInstance("threadLocalStatic");
    public ThreadLocal call() throws Exception {
        TestWithThreadLocalStatic.getThreadLocalStatic().set("1");
        Thread.sleep(1000);
        instance.show();
        return TestWithThreadLocalStatic.getThreadLocalStatic();
    }
}


class TestWithThreadLocalStatic extends DefaultThreadLocal{
    private TestWithThreadLocalStatic(){}
    private static volatile TestWithThreadLocalStatic instance;
    public static TestWithThreadLocalStatic getInstance(){
        if(instance == null){
            synchronized ((TestWithThreadLocalStatic.class)){
                if(instance == null){
                    instance = new TestWithThreadLocalStatic();
                }
            }
        }
        return  instance;
    }

    public void show() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        System.out.println("线程"+threadName+"值是:"+ TestWithThreadLocalStatic.getThreadLocalStatic().get());
    }
}





// ************* 测试2 使用 ThreadLocal ***************
// 使用 static ThreadLocal都导致线程1取到了 线程2设置的值2； 测试成功。
class ThreadLocalRun {
    public static void main(String[] args) throws Exception {
        // 线程1
        FutureTask<ThreadLocal> futureTask = new FutureTask<ThreadLocal>(new MyCallableThreadLocal());
        Thread thread01 = new Thread(futureTask);
        thread01.setName("thread01");
        thread01.start();
        // 线程2
        FutureTask<ThreadLocal> futureTask2 = new FutureTask<ThreadLocal>(new MyCallable2ThreadLocal());
        Thread thread02 = new Thread(futureTask2);
        thread02.setName("thread02");
        thread02.start();
        try {
            ThreadLocal ins1 = futureTask.get();
            System.out.println(ins1);
            ThreadLocal ins2 = futureTask2.get();
            System.out.println(ins2);
            System.out.println(ins1 == ins2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}



// 线程2任务，把str值设置成2；实际场景中这个两个任务其实是同一方法，值来自不同客户端的入参。
class MyCallable2ThreadLocal implements Callable<ThreadLocal>{
    DefaultThreadLocal instance = InstanceFactory.CreateInstance("threadLocalStatic");
    public ThreadLocal call() throws Exception {
        instance.getThreadLocal().set("2");
        instance.show();
        return instance.getThreadLocal();
    }
}

// 线程1任务，把str值设置成1；
class MyCallableThreadLocal implements Callable<ThreadLocal>{
    DefaultThreadLocal instance = InstanceFactory.CreateInstance("threadLocal");
    public ThreadLocal call() throws Exception {
        instance.getThreadLocal().set("1");
        Thread.sleep(1000);
        instance.show();
        return instance.getThreadLocal(); //返回他出去 比较是否单例；
    }
}

// 测试使用ThreadLocal
class TestWithThreadLocal extends DefaultThreadLocal{
    private TestWithThreadLocal(){}
    private static volatile TestWithThreadLocal instance;
    public static TestWithThreadLocal getInstance(){
        if(instance == null){
            synchronized ((TestWithThreadLocal.class)){
                if(instance == null){
                    instance = new TestWithThreadLocal();
                }
            }
        }
        return  instance;
    }

    public void show() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        System.out.println("线程"+threadName+"值是:"+super.getThreadLocal().get());
    }
}



// *************** 测试3 没有使用ThreadLocal ***************************
// 没有使用ThreadLocal都导致线程1取到了 线程2设置的值2； 测试成功。
public class StaticProtogenesis {
    public static void main(String[] args) throws Exception {
        // 线程1
        FutureTask<DefaultThreadLocal> futureTask = new FutureTask<DefaultThreadLocal>(new MyCallable());
        Thread thread01 = new Thread(futureTask);
        thread01.setName("thread01");
        thread01.start();
        // 线程2
        FutureTask<DefaultThreadLocal> futureTask2 = new FutureTask<DefaultThreadLocal>(new MyCallable2());
        Thread thread02 = new Thread(futureTask2);
        thread02.setName("thread02");
        thread02.start();
        try {
            DefaultThreadLocal ins1 = futureTask.get();
            System.out.println(ins1);
            DefaultThreadLocal ins2 = futureTask2.get();
            System.out.println(ins2);
            System.out.println(ins1 == ins2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}


// 线程2任务，把str值设置成2；
class MyCallable2 implements Callable<DefaultThreadLocal>{
    DefaultThreadLocal instance = InstanceFactory.CreateInstance("noThreadLocal");
    public DefaultThreadLocal call() throws Exception {
        instance.setStr("2");
        instance.show();
        return instance; //返回他出去 比较是否单例；
    }
}

// 线程1任务，把str值设置成1；
class MyCallable implements Callable<DefaultThreadLocal>{
    // 注入测试ThreadLocal 确保单例
    DefaultThreadLocal instance = InstanceFactory.CreateInstance("noThreadLocal");
    public DefaultThreadLocal call() throws Exception {
        instance.setStr("1");
        instance.show();
        return instance; //返回他出去 比较是否单例；
    }
}

// 测试不用ThreadLocal 单例模式
class TestNoThreadLocal extends DefaultThreadLocal{
    private TestNoThreadLocal(){}
    private static volatile TestNoThreadLocal instance;
    public static TestNoThreadLocal getInstance(){
        // 第一次判断
        if(instance == null){
            // 锁前区域
            synchronized ((TestNoThreadLocal.class)){
                // 线程1，线程2都进入到锁前区域，即是线程1，线程2都会同时通过第一次判断。所以要加第二个判断。
                // 第二判断也叫锁后判断。
                // 从双重校验第二次判断(锁后判断)了解并发,了解拿锁后再判断必要性
                // 第二次判断
                if(instance == null){
                    instance = new TestNoThreadLocal();
                }
            }

        }
        return  instance;
    }

    public void show() throws InterruptedException {
        String threadName = Thread.currentThread().getName();
        if(threadName.equals("thread01")){
            Thread.sleep(1000);
        }
        System.out.println("线程"+threadName+"值是:"+super.getStr());
    }
}

// ***********************************************************
// 简单测试一下static ThreadLocal ，结果报错，所以带static不可以吗？
class StaticTest {
    public static ThreadLocal<String> threadLocal;
    public StaticTest(String val) {
        StaticTest.threadLocal.set(val);
    }

    public void show(){
        System.out.println(StaticTest.threadLocal.get());
    }
}

// 简单测试一下static
class StaticTest2 {
    private static String val;
    public StaticTest2(String val){
        StaticTest2.val = val;
    }

    public void show(){
        System.out.println(StaticTest2.val);
    }

}
