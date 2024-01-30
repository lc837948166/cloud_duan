package com.xw.cloud.Utils;


import com.xw.cloud.bean.*;
import lombok.SneakyThrows;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * 硬件工具类
 *
 */
public class HardWareUtil {

    private HardWareUtil() {}

    /**
     * 等待休眠时间，单位ms
     */
    private static final int WAIT_TIME_MS = 1000;

    /**
     * 获取cpu信息
     *
     * @return  cpu信息
     */
    public static CpuInfo getCpuInfo() {
        oshi.SystemInfo si = new oshi.SystemInfo();
        CentralProcessor processor = si.getHardware().getProcessor();
        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(WAIT_TIME_MS);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softIrq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long cSys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long ioWait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + ioWait + irq + softIrq + steal;
        CpuInfo cpuInfo = new CpuInfo();
        cpuInfo.setCpuNum(processor.getLogicalProcessorCount());
        cpuInfo.setTotal(totalCpu);
        cpuInfo.setSys(Double.parseDouble(new DecimalFormat("#.####").format(cSys * 1.0 / totalCpu))*100);
        cpuInfo.setUsed(Double.parseDouble(new DecimalFormat("#.####").format(user * 1.0 / totalCpu))*100);
        cpuInfo.setWait(Double.parseDouble(new DecimalFormat("#.####").format(ioWait * 1.0 / totalCpu))*100);
        cpuInfo.setFree(Double.parseDouble(new DecimalFormat("#.####").format(idle * 1.0 / totalCpu))*100);
        return cpuInfo;
    }

    /**
     * 获取内存信息
     *
     * @param size  大小单位，默认为B
     * @return      内存信息
     */
    public static MemoryInfo getMemoryInfo(SizeEnum size) {
        SystemInfo si = new SystemInfo();
        GlobalMemory memory = si.getHardware().getMemory();
        // 内存信息
        MemoryInfo mem = new MemoryInfo();
        mem.setTotal((double) Math.round(Objects.isNull(size) ? memory.getTotal() : (float) memory.getTotal() / size.getSize() * 100) /100);
        mem.setUsed((double) Math.round(Objects.isNull(size) ? (memory.getTotal() - memory.getAvailable()) : (float) (memory.getTotal() - memory.getAvailable()) / size.getSize() * 100) /100);
        mem.setFree((double) Math.round(Objects.isNull(size) ? memory.getAvailable() : (float) memory.getAvailable() / size.getSize() * 100) /100);
        return mem;
    }

    /**
     * 获取服务器信息
     *
     * @return  服务器信息
     */
    public static SystemDetails getSystemInfo() throws UnknownHostException {
        InetAddress ip = InetAddress.getLocalHost();
        SystemDetails details = new SystemDetails();
        SystemInfo systemInfo = new SystemInfo();
        Properties props = System.getProperties();
        details.setComputerName(ip.getHostName());
        details.setComputerIp(ip.getHostAddress());
        details.setOsName(props.getProperty("os.name"));
        details.setOsArch(props.getProperty("os.arch"));
        details.setUserDir(props.getProperty("user.dir"));
        details.setOsBuild(systemInfo.getHardware().getProcessor().getProcessorIdentifier().getName());
        details.setOsVersion(String.valueOf(systemInfo.getOperatingSystem().getVersionInfo()));
        return details;
    }

    /**
     * 获取Java虚拟机信息
     */
    public static JvmInfo getJvmInfo() {
        JvmInfo jvmInfo = new JvmInfo();
        Properties props = System.getProperties();
        jvmInfo.setTotal( (double) Math.round(Runtime.getRuntime().totalMemory() / (1024.0 * 1024.0) * 100) /100);
        jvmInfo.setMax((double) Math.round(Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0) * 100) /100);
        jvmInfo.setFree((double) Math.round(Runtime.getRuntime().freeMemory() / (1024.0 * 1024.0) * 100) /100);
        jvmInfo.setVersion(props.getProperty("java.version"));
        jvmInfo.setHome(props.getProperty("java.home"));
        return jvmInfo;
    }

    /**
     * 获取磁盘信息
     */
    public static List<SysFile> getSysFiles() {
        SystemInfo si = new SystemInfo();
        // 获取操作系统
        OperatingSystem operatingSystem = si.getOperatingSystem();
        // 获取操作系统的文件系统
        FileSystem fileSystem = operatingSystem.getFileSystem();
        // 获取文件存储信息
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        List<SysFile> sysFiles = new ArrayList<>();
        for (OSFileStore fs : fsArray) {
            // 获取可用空间
            long free = fs.getUsableSpace();
            // 获取总空间
            long total = fs.getTotalSpace();
            long used = total - free;
            SysFile sysFile = new SysFile();
            sysFile.setDirName(fs.getMount());
            sysFile.setSysTypeName(fs.getType());
            sysFile.setTypeName(fs.getName());
            sysFile.setTotal(convertFileSize(total));
            sysFile.setFree(convertFileSize(free));
            sysFile.setUsed(convertFileSize(used));
            sysFile.setUsage(mul(div(used, total, 4), 100));
            sysFiles.add(sysFile);
        }
        return sysFiles;
    }


    public static String convertFileSize(long size) {
        if (size >= SizeEnum.GB.getSize()) {
            return String.format("%.1f GB", (float) size / SizeEnum.GB.getSize());
        } else if (size >= SizeEnum.MB.getSize()) {
            float f = (float) size / SizeEnum.MB.getSize();
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= SizeEnum.KB.getSize()) {
            float f = (float) size / SizeEnum.KB.getSize();
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else {
            return String.format("%d B", size);
        }
    }


    public static double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }


    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        if (b1.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.doubleValue();
        }
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 获取物理机信息
     * @return MachineInfo
     */
    @SneakyThrows
    public static MachineInfo getMachineInfo(){
        return MachineInfo.builder()
                .cpuInfo(getCpuInfo())
                .jvmInfo(getJvmInfo())
                .systeminfo(getSystemInfo())
                .memoryInfo(getMemoryInfo(SizeEnum.GB))
                .sysFiles(getSysFiles()).build();
    }
}