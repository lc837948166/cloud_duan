package com.xw.cloud.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xw.cloud.Utils.LibvirtUtils;
import com.jcraft.jsch.*;
import com.xw.cloud.Utils.*;
import com.xw.cloud.bean.*;
import com.xw.cloud.bean.NodeInfo;
import com.xw.cloud.mapper.IpaddrMapper;
import com.xw.cloud.mapper.NodeMapper;
import com.xw.cloud.mapper.VmMapper;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.libvirt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import com.xw.cloud.Utils.SftpUtils;
@Log
@Service(value = "libvirtService")
public class LibvirtService {
    @Resource
    private VmMapper vmMapper;
    @Resource
    private NodeMapper nodeMapper;

    static int[] arrayPort = {8000, 8085, 8086, 7051, 7052, 7053};

    @Resource
    private IpaddrMapper ipaddrMapper;

    String home = System.getenv("HOME");
    /**
     * getHostInformation
     */
    public Host getHostInfo() {
        return SigarUtils.getHostInfo();
    }

    /**
     * getLibvirtConnectInformation
     */
    public LibvirtConnect getLibvirtConnectInformation() {
        return LibvirtUtils.getConnectionIo();
    }

    /**
     * getDomainById
     */
    @SneakyThrows
    public Domain getDomainById(int id) {
        return LibvirtUtils.getConnection().domainLookupByID(id);
    }

    /**
     * getDomainByName
     */
    @SneakyThrows
    public Domain getDomainByName(String name) {
        return LibvirtUtils.getConnection().domainLookupByName(name);
    }

    /**
     * getVirtualById
     */
    @SneakyThrows
    public Virtual getVirtualById(int id) {
        Domain domain = getDomainById(id);
        return Virtual.builder()
                .id(domain.getID())
                .name(domain.getName())
                .state(domain.getInfo().state.toString())
                .maxMem(domain.getMaxMemory() >>20)
                .cpuNum(domain.getMaxVcpus())
                .ipaddr(getVMip(domain.getName()))
                .build();
    }



    /**
     * getVirtualByName
     */
    @SneakyThrows
    public Virtual getVirtualByName(String name) {
        Domain domain = getDomainByName(name);
        String ip = null;
        VMInfo2 vmInfo2 = vmMapper.selectById(domain.getName());
        if (vmInfo2 != null) {
            ip = vmInfo2.getIp();
        }
//        String ip=vmMapper.selectById(domain.getName()).getIp();
        return Virtual.builder()
                .id(domain.getID())
                .name(domain.getName())
                .ipaddr(getVMip(name))
                .state(domain.getInfo().state.toString())
                .ipaddr(ip)
                .build();
    }



    /**
     * 虚拟机列表
     */
    @SneakyThrows
    public List<Virtual> getVirtualList() {
        ArrayList<Virtual> virtualList = new ArrayList<>();
        // live
        int[] ids = LibvirtUtils.getConnection().listDomains();
        for (int id : ids) virtualList.add(getVirtualById(id));
        // down
        String[] names = LibvirtUtils.getConnection().listDefinedDomains();
        for (String name : names) virtualList.add(getVirtualByName(name));
        return virtualList;
    }

    @SneakyThrows
    public String getVMip(String name) {
//        String command = "for mac in `sudo virsh domiflist "+name+" |grep -o -E \"([0-9a-f]{2}:){5}([0-9a-f]{2})\"` ; do arp -e | grep $mac  | grep -o -P \"^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\" ; done";
//        String ip =SftpUtils.getexecon(command);
        String ip = null;
        VMInfo2 vmInfo2 = vmMapper.selectById(name);
        if (vmInfo2 != null) {
            ip = vmInfo2.getIp();
        }
        System.out.println(ip);
        return ip;
    }

    @SneakyThrows
    public void getallVMip(String serverip) {
        String ip1=findserverip(findRealIP(serverip),'.',3);

        String command="bash /home/qemuVM/virsh-ip.sh all "+ip1;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        Process process = processBuilder.start();

        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        StringBuilder commandOutput = new StringBuilder();
        String line2;
        while ((line2 = bufferedReader.readLine()) != null) {
            commandOutput.append(line2).append("\n");
        }

        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();
        boolean processExited = process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);

        if (processExited) {
            int exitCode = process.exitValue();
            System.out.println("命令执行完成，退出码：" + exitCode);
        } else {
            // 超时处理
            process.destroy();
            System.out.println("命令执行超时");
        }

        StringReader stringReader = new StringReader(commandOutput.toString());
        BufferedReader reader = new BufferedReader(stringReader);
        String line;
        int result = 0;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String name = parts[0];
                String ip = parts[1].trim().substring(0, Math.min(parts[1].trim().length(), 15));
                if (!ip.isEmpty()) {
                    VMInfo2 virtualMachine = new VMInfo2();
                    virtualMachine.setName(name);
                    virtualMachine.setIp(ip);
                    result+=vmMapper.updateById(virtualMachine);
                }
            }
        }
    }



    public String findserverip(String str, char c, int n) {
        int index = -1;
        for (int i = 0; i < n; i++) {
            index = str.indexOf(c, index + 1);
            if (index == -1) {
                break;
            }
        }
        return str.substring(0, index);
    }


    /**
     * 虚拟机指标列表
     */
    @SneakyThrows
    public List<Virtual> getIndexList() {
        ArrayList<Virtual> virtualList = new ArrayList<>();
        // live
        int[] ids = LibvirtUtils.getConnection().listDomains();
        for (int id : ids) virtualList.add(getVirtualByLive(id));
        return virtualList;
    }
    @SneakyThrows
    public Virtual getIndex(String name,Integer up,Integer down) {
        return getIndexByName(name,up,down);
    }

    @SneakyThrows
    public Virtual getIndexByName(String name,Integer up,Integer down) {
        Domain domain = getDomainByName(name);
//        String data =SftpUtils.getexecon("virsh domiflist "+name);
//        StringReader stringReader = new StringReader(data);
//        BufferedReader reader = new BufferedReader(stringReader);
//        String line;
//        boolean headerFound = false;
//        String interfaceValue = null;
//
//        // 解析输出
//        while ((line = reader.readLine()) != null) {
//            if (!headerFound) {
//                if (line.contains("Interface")) {
//                    headerFound = true;
//                }
//                continue;
//            }
//
//            if (!line.trim().isEmpty()) {
//                // 提取Interface列的信息
//                String[] columns = line.split("\\s+");
//                if (columns.length > 1) {
//                    interfaceValue = columns[0];
//                    break;
//                }
//            }
//        }
//        // 关闭流和进程
//        reader.close();
//        // 输出Interface的值
//        if (interfaceValue != null) {
//            System.out.println("Interface: " + interfaceValue);
//        }
//
//        DomainInterfaceStats stats1 = domain.interfaceStats(interfaceValue);
//        Thread.sleep(1000);
//        DomainInterfaceStats stats2 = domain.interfaceStats(interfaceValue);
//        long bandwidth = (stats2.rx_bytes - stats1.rx_bytes + stats2.tx_bytes-stats1.tx_bytes)  / 15625;
//        System.out.printf("当前带宽大小为：%d Mbps", bandwidth);
        return Virtual.builder()
                .id(domain.getID())
                .name(name)
                .state(domain.getInfo().state.toString())
                .maxMem(domain.getMaxMemory()  >>20)
                .useMem(getMem(domain))
                .cpuNum(domain.getMaxVcpus())
                .usecpu(getCpu(domain))
//                .bandwidth(bandwidth)
                .ipaddr(getVMip(domain.getName()))
                .downBW(down)
                .upBW(up)
                .build();
    }

    @SneakyThrows
    public Virtual getVirtualByLive(int id) {
        Domain domain = getDomainById(id);
//        DomainBlockInfo blockInfo = domain.blockInfo(home+"/VM_place/"+domain.getName()+".img");
//        long totalSize = blockInfo.getCapacity();
        return Virtual.builder()
                .id(domain.getID())
                .name(domain.getName())
                .state(domain.getInfo().state.toString())
                .maxMem(domain.getMaxMemory()  >>20)
                .useMem(getMem(domain))
                .cpuNum(domain.getMaxVcpus())
                .usecpu(getCpu(domain))
                .ipaddr(getVMip(domain.getName()))
                .build();
    }

    @SneakyThrows
    public double getMem(Domain domain){
        double useMem = 0;
        MemoryStatistic[] memoryStatistics = domain.memoryStats(9);
        Optional<MemoryStatistic> first = Arrays.stream(memoryStatistics).filter(x -> x.getTag() == 5).findFirst();
        if (first.isPresent()) {
            MemoryStatistic memoryStatistic = first.get();
            long unusedMemory = memoryStatistic.getValue();
            long maxMemory = domain.getMaxMemory();
            useMem = (maxMemory - unusedMemory) * 100.0 / maxMemory;
        }
        double truncatedValue = (int) (useMem * 100) / 100.0;
        return truncatedValue;
    }
    @SneakyThrows
    public double getCpu(Domain domain){
        long c1 = domain.getInfo().cpuTime;
        Thread.sleep(1000);
        long c2 = domain.getInfo().cpuTime;
        int vCpus = domain.getMaxVcpus();
        Double cpuUsage = 100 * (c2 - c1) / (1 * vCpus * Math.pow(10, 9));
        System.out.println(cpuUsage);
        return cpuUsage;
    }




    /**
     * 暂停/挂起 虚拟机
     */
    @SneakyThrows
    private void suspendedDomain(Domain domain) {
        if (domain.isActive() == 1) {
            domain.suspend();
            log.info(domain.getName() + "虚拟机已挂起！");
        } else log.info("虚拟机未打开");
    }

    public void suspendedDomainById(int id) {
        suspendedDomain(getDomainById(id));
    }

    public void suspendedDomainName(String name) {
        suspendedDomain(getDomainByName(name));
    }


    /**
     * 还原 暂停/挂起 虚拟机
     */
    @SneakyThrows
    private void resumeDomain(Domain domain) {
        if (domain.isActive() == 1) {
            domain.resume();
            log.info(domain.getName() + "虚拟机已唤醒！");
        } else log.info("虚拟机未打开");
    }

    public void resumeDomainById(int id) {
        resumeDomain(getDomainById(id));
    }

    public void resumeDomainByName(String name) {
        resumeDomain(getDomainByName(name));
    }

    /**
     * 保存 虚拟机 --->img文件
     */
    @SneakyThrows
    private void saveDomain(Domain domain) {
        JFileChooser jf = new JFileChooser();
        jf.setFileSelectionMode(JFileChooser.SAVE_DIALOG | JFileChooser.DIRECTORIES_ONLY);
        jf.showDialog(null, null);
        String f = jf.getSelectedFile().getAbsolutePath() + "/save.qcow2";
        if (domain.isActive() == 1) {
            domain.save(f);
            log.info(domain.getName() + "虚拟机状态已保存！" + "save: " + f);
        } else log.info("虚拟机未打开");
    }

    public void saveDomainById(int id) {
        saveDomain(getDomainById(id));
    }

    public void saveDomainByName(String name) {
        saveDomain(getDomainByName(name));
    }

    /**
     * 恢复 虚拟机 --->img文件
     */
    @SneakyThrows
    private void restoreDomain(Domain domain) {
        JFileChooser chooser = new JFileChooser();
        chooser.showOpenDialog(null);
        if (chooser.getSelectedFile() != null) {
            String path = chooser.getSelectedFile().getPath();
            if (domain.isActive() == 0) {
                domain.getConnect().restore(path);
                log.info(domain.getName() + "虚拟机状态已恢复！！" + "path: " + path);
            } else log.info("虚拟机未关闭");
        }
    }

    public void restoreDomainById(int id) {
        restoreDomain(getDomainById(id));
    }

    public void restoreDomainByName(String name) {
        restoreDomain(getDomainByName(name));
    }

    /**
     * 启动 虚拟机
     */
    @SneakyThrows
    private void initiateDomain(Domain domain) {
        if (domain.isActive() == 0) {
            domain.create();
            log.info(domain.getName() + "虚拟机已启动！");
        } else log.info("虚拟机已经打开过！");
    }

    public void initiateDomainByName(String name) {
        initiateDomain(getDomainByName(name));
    }

    /**
     * 关闭 虚拟机
     */
    @SneakyThrows
    private void shutdownDomain(Domain domain) {
        if (domain.isActive() == 1) {
            domain.shutdown();
            log.info(domain.getName() + "虚拟机已正常关机！");
        } else log.info("虚拟机未打开");
    }

    public void shutdownDomainById(int id) {
        shutdownDomain(getDomainById(id));
    }

    public void shutdownDomainByName(String name) throws LibvirtException {
            shutdownDomain(getDomainByName(name));
    }

    public void changeVMByName(String name,int mem,int cpu) throws LibvirtException {
        VMInfo2 vm = new VMInfo2();
        vm.setName(name);
        vm.setCpuNum(cpu);
        vm.setMemory(mem);

         mem = mem * 1024 * 1024;
        Domain domain= getDomainByName(name);
        String xmlDesc = domain.getXMLDesc(0);
        xmlDesc = xmlDesc.replaceAll("<vcpu.*?</vcpu>", "<vcpu placement='static'>" + cpu + "</vcpu>");
        xmlDesc = xmlDesc.replaceAll("<memory unit='KiB'>.*?</memory>", "<memory unit='KiB'>" + mem + "</memory>");
        xmlDesc = xmlDesc.replaceAll("<currentMemory unit='KiB'>.*?</currentMemory>", "<currentMemory unit='KiB'>" + mem + "</currentMemory>");
        domain.undefine();
        domain=LibvirtUtils.getConnection().domainDefineXML(xmlDesc);
        initiateDomain(domain);

        vmMapper.updateById(vm);
    }

    /**
     * 强制关闭 虚拟机
     */
    @SneakyThrows
    private void shutdownMustDomain(Domain domain) {
        if (domain.isActive() == 1) {
            domain.destroy();
            log.info(domain.getName() + "虚拟机已强制关机！");
        } else log.info("虚拟机未打开");
    }

    public void shutdownMustDomainById(int id) {
        shutdownMustDomain(getDomainById(id));
    }

    public void shutdownMustDomainByName(String name) {
        shutdownMustDomain(getDomainByName(name));
    }

    /**
     * 重启 虚拟机
     */
    @SneakyThrows
    private void rebootDomain(Domain domain) {
        if (domain.isActive() == 1) {
            domain.reboot(0);
            log.info(domain.getName() + "虚拟机状态已重启！");
        } else log.info("虚拟机未打开");
    }

    public void rebootDomainById(int id) {
        rebootDomain(getDomainById(id));
    }

    public void rebootDomainByName(String name) {
        rebootDomain(getDomainByName(name));
    }

    /**
     * 添加 虚拟机 xml------>name   1024MB
     */
    @SneakyThrows
    public void addDomainByName(VM_create vmc,String serverip) {
        String xml = "<domain type='kvm'>\n" +
                "  <name>" + vmc.getName() + "</name>\n" +
                "  <uuid>" + UUID.randomUUID() + "</uuid>\n" +
                "  <memory unit='GiB'>"+vmc.getMemory()+"</memory>\n" +                 // 1024 MB
                "  <currentMemory unit='GiB'>"+vmc.getMemory()+"</currentMemory>\n" +   // 1024 MB same with up
                "  <vcpu placement='static'>"+vmc.getCpuNum()+"</vcpu>\n";
                if(vmc.getOStype().equals("arm")){
                    xml+="  <os>\n" +
                         "    <type arch='aarch64' machine='virt'>hvm</type>\n" +
                         "    <boot dev='hd'/>\n" +
                         "  </os>\n";}
                else if (vmc.getOStype().equals("X86")){
                    xml+=   "  <os>\n" +
                            "    <type arch='x86_64' machine='pc'>hvm</type>\n" +
                            "    <boot dev='hd'/>\n" +
                            "  </os>\n";}
                xml+=
                "  <features>\n" +
                "    <acpi/>\n" +
                "    <apic/>\n" +
//                "    <vmport state='off'/>\n" +
                "  </features>\n" +
                "  <cpu mode='host-model' check='partial'/>\n" +
                "  <clock offset='utc'>\n" +
                "    <timer name='rtc' tickpolicy='catchup'/>\n" +
                "    <timer name='pit' tickpolicy='delay'/>\n" +
                "    <timer name='hpet' present='no'/>\n" +
                "  </clock>\n" +
                "  <on_poweroff>destroy</on_poweroff>\n" +
                "  <on_reboot>restart</on_reboot>\n" +
                "  <on_crash>destroy</on_crash>\n" +
                "  <pm>\n" +
                "    <suspend-to-mem enabled='no'/>\n" +
                "    <suspend-to-disk enabled='no'/>\n" +
                "  </pm>\n" +
                "  <devices>\n" +
                "    <emulator>" + "/usr/libexec/qemu-kvm" + "</emulator>\n" +
                "    <disk type='file' device='disk'>\n" +
                "      <driver name='qemu' type='qcow2'/>\n" +
                "      <source file='/home/qemuVM/VM_place/" + vmc.getName() +".qcow2'"+"/>\n" +   // FileSource
                        "      <target dev='hdb' bus='ide'/>\n" +
                "      <address type='drive' controller='0' bus='0' target='0' unit='0'/>\n" +
                        "    </disk>\n" +
                "    <controller type='usb' index='0' model='ich9-ehci1'>\n" +
                "      <address type='pci' domain='0x0000' bus='0x00' slot='0x05' function='0x7'/>\n" +
                "    </controller>\n" +
                "    <controller type='usb' index='0' model='ich9-uhci1'>\n" +
                "      <master startport='0'/>\n" +
                "      <address type='pci' domain='0x0000' bus='0x00' slot='0x05' function='0x0' multifunction='on'/>\n" +
                "    </controller>\n" +
                "    <controller type='usb' index='0' model='ich9-uhci2'>\n" +
                "      <master startport='2'/>\n" +
                "      <address type='pci' domain='0x0000' bus='0x00' slot='0x05' function='0x1'/>\n" +
                "    </controller>\n" +
                "    <controller type='usb' index='0' model='ich9-uhci3'>\n" +
                "      <master startport='4'/>\n" +
                "      <address type='pci' domain='0x0000' bus='0x00' slot='0x05' function='0x2'/>\n" +
                "    </controller>\n" +
                "    <controller type='pci' index='0' model='pci-root'/>\n" +
                "    <controller type='virtio-serial' index='0'>\n" +
                "      <address type='pci' domain='0x0000' bus='0x00' slot='0x06' function='0x0'/>\n" +
                "    </controller>\n";
        if(vmc.getNetType().equals("bridge")){
            xml+="    <interface type='bridge'>\n" +
                    "      <source bridge='br0'/>\n" +
                    "      <model/>\n" +
                    "      <address type='pci' domain='0x0000' bus='0x00' slot='0x03' function='0x0'/>\n" +
                    "    </interface>";
        }
        else if (vmc.getNetType().equals("nat")) {
            xml += "    <interface type='network'>\n" +
                    "      <source network='default'/>\n" +
                    "      <model/>\n" +
                    "      <address type='pci' domain='0x0000' bus='0x00' slot='0x03' function='0x0'/>\n" +
                    "    </interface>\n";
        }
                xml+="    <serial type='pty'>\n" +
                "      <target type='isa-serial' port='0'>\n" +
                "        <model name='isa-serial'/>\n" +
                "      </target>\n" +
                "    </serial>\n" +
                "    <console type='pty'>\n" +
                "      <target type='serial' port='0'/>\n" +
                "    </console>\n" +
                "    <channel type='spicevmc'>\n" +
                "      <target type='virtio' name='com.redhat.spice.0'/>\n" +
                "      <address type='virtio-serial' controller='0' bus='0' port='1'/>\n" +
                "    </channel>\n" +
                        "<channel type='unix'>\n" +
                        "  <source mode='bind'/>\n" +
                        "  <target type='virtio' name='org.qemu.guest_agent.0'/>\n" +
                        "</channel>"+
                "    <input type='tablet' bus='usb'>\n" +
                "      <address type='usb' bus='0' port='1'/>\n" +
                "    </input>\n" +
                "    <input type='mouse' bus='ps2'/>\n" +
                "    <input type='keyboard' bus='ps2'/>\n" +
                "    <graphics type='vnc' port='-1' autoport='yes' listen='0.0.0.0' keymap='en-us'>\n" +
                "      <listen type='address' address='0.0.0.0'/>\n" +
                "    </graphics>\n" +
                "    <sound model='ich6'>\n" +
                "      <address type='pci' domain='0x0000' bus='0x00' slot='0x04' function='0x0'/>\n" +
                "    </sound>\n" +
                "    <video>\n" +
                "      <model type='qxl' ram='65536' vram='65536' vgamem='16384' heads='1' primary='yes'/>\n" +
                "      <address type='pci' domain='0x0000' bus='0x00' slot='0x02' function='0x0'/>\n" +
                "    </video>\n" +
                "    <redirdev bus='usb' type='spicevmc'>\n" +
                "      <address type='usb' bus='0' port='2'/>\n" +
                "    </redirdev>\n" +
                "    <redirdev bus='usb' type='spicevmc'>\n" +
                "      <address type='usb' bus='0' port='3'/>\n" +
                "    </redirdev>\n" +
                "    <memballoon model='virtio'>\n" +
                "      <address type='pci' domain='0x0000' bus='0x00' slot='0x08' function='0x0'/>\n" +
                "    </memballoon>\n" +
                "  </devices>\n" +
                "</domain>";
        LibvirtUtils.getConnection().domainDefineXML(xml);    // define ------> creat
        log.info(vmc.getName() + "虚拟机已创建！");
        Thread.sleep(1000);
        initiateDomainByName(vmc.getName());
        updateVMtable(vmc.getName(),serverip,vmc.getCpuNum(),vmc.getMemory());
        Thread.sleep(6000);
            getallVMip(serverip);
            for (int i = 0; i < 60 ; ++i) {
                if (vmMapper.selectById(vmc.getName()).getIp() == null || vmMapper.selectById(vmc.getName()).getIp().isEmpty()){
                    Thread.sleep(10000);
                    getallVMip(serverip);
                }
                else break;
                }
        updateNIC(vmc.getName(),vmMapper.selectById(vmc.getName()).getIp());

            }

     public void updateNIC(String name,String ip){
         VMInfo2 vmInfo2 = new VMInfo2();
         vmInfo2.setName(name);
         String data= SftpUtils.getexecon1(ip,"cat /proc/net/dev | awk '{i++; if(i>2){print $1}}' | sed 's/^[\\t]*//g' | sed 's/[:]*$//g'");
         String[] lines = data.split("\\r?\\n");
         String nic="eth0";
         for (int i = 0; i < lines.length; i++) {
             String str = lines[i];
             if (str.charAt(0) == 'e') {
                 System.out.println("Index: " + i + ", String: " + str);
                 nic=str;
             }
         }
         vmInfo2.setNic(nic);
         vmMapper.updateById(vmInfo2);
     }


    /**
     * 更新数据库的虚拟机信息
     */
    @SneakyThrows
    private void updateVMtable(String name,String serverip,int cpu,int memory) {

        VMInfo2 vmInfo2=VMInfo2.builder()
                .name(name)
                .username("root")
                .passwd("111")
                .memory(memory)
                .cpuNum(cpu)
                .serverip(serverip).build();
        vmMapper.insert(vmInfo2);
    }

    public String findRealIP(String serverip){
        String realip=null;
        Ipaddr ip= ipaddrMapper.selectById(serverip);
        if(ip!=null) realip=ip.getRealip();
        return realip;
    }


    //添加端口映射
    @SneakyThrows
    public void addport(String name){
        String ip=vmMapper.selectById(name).getIp();
        List<Integer> availablePorts = findAvailablePortSequence(12345,6);

        VMInfo2 vm = new VMInfo2();
        vm.setName(name);
        vm.setHostport(availablePorts.get(0));
        System.out.println(vm.getHostport());
        int result=vmMapper.updateById(vm);
        System.out.println("Update result: " + result);

        System.out.println("Available first Port: " + availablePorts.get(0).toString());
        String endCommand="pkill rinetd";
        SftpUtils.getexecon(endCommand);

        StringBuilder addCommands = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            addCommands.append(String.format("echo '0.0.0.0 %d  %s %d' >> /etc/rinetd.conf && ", availablePorts.get(i), ip, arrayPort[i]));
        }
        String combinedCommand = addCommands.toString();
        combinedCommand = combinedCommand.substring(0, combinedCommand.length() - 4);
        SftpUtils.getexecon(combinedCommand);

        String startCommand = "rinetd -c /etc/rinetd.conf";
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", startCommand);
        Process process = processBuilder.start();

// 等待命令执行完成，设置超时时间为3秒
        boolean processExited = process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);

        if (processExited) {
            int exitCode = process.exitValue();
            System.out.println("命令执行完成，退出码：" + exitCode);
        } else {
            // 超时处理
            process.destroy();
            System.out.println("命令执行超时");
        }
    }

    private static List<Integer> findAvailablePortSequence(int startingPort, int sequenceLength) {
        List<Integer> availablePorts = new ArrayList<>();
        int port = startingPort;

        while (availablePorts.size() < sequenceLength) {
            if (isPortAvailable(port)) {
                availablePorts.add(port);
                port++;
            } else {
                availablePorts.clear();
                port += sequenceLength;
            }
        }

        return availablePorts;
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @SneakyThrows
    public void deletePort(String name) {
        String targetIpAddress =vmMapper.selectById(name).getIp();
        Integer hostport = vmMapper.selectById(name).getHostport();
        if (hostport != null) {
        String filePath = "/etc/rinetd.conf";
        int linesToRemove = 6;

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder contentBuilder = new StringBuilder();
        String line;
        int linesRemoved = 1;

        while ((line = reader.readLine()) != null && linesRemoved < linesToRemove) {
            if (line.contains(targetIpAddress)) {
                linesRemoved++;
            } else {
                contentBuilder.append(line).append(System.lineSeparator());
            }
        }

        String remainingLines;
        while ((remainingLines = reader.readLine()) != null) {
            contentBuilder.append(remainingLines).append(System.lineSeparator());
        }

        reader.close();
        String content = contentBuilder.toString();

        // 将修改后的内容写回文件
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
        writer.write(content);
        writer.close();

        String endCommand="pkill rinetd";
        SftpUtils.getexecon(endCommand);
        String startCommand = "rinetd -c /etc/rinetd.conf";
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", startCommand);
        Process process = processBuilder.start();
        }
    }



    /**
     * 删除 虚拟机 xml
     */
    @SneakyThrows
    private void deleteDomain(Domain domain) {
        if (domain.isActive() == 1) domain.destroy();  // 强制关机
        domain.undefine();
        log.info(domain.getName() + "虚拟机已删除！");
    }

    public void deleteDomainById(int id) {
        deleteDomain(getDomainById(id));
    }

    public void deleteDomainByName(String name) {
        deleteDomain(getDomainByName(name));
        vmMapper.deleteById(name);
    }

    /**
     * get ImgList

    /**
     * 添加 img
     */
    @SneakyThrows
    public void addImgFile(String name,String ImgName) {
        File sourceFile = new File("/home/qemuVM/images/"+ImgName);
        File destinationFile = new File("/home/qemuVM/VM_place/"+name+".qcow2");
        try {
            copyFile(sourceFile, destinationFile);
        } catch (IOException e) {
            System.out.println("An error occurred while copying the file: " + e.getMessage());
        }

    }

    public void copyFile(File source, File destination) throws IOException {

        try (FileInputStream inputStream = new FileInputStream(source); FileOutputStream outputStream = new FileOutputStream(destination)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 删除 img
     */
    @SneakyThrows
    public void deleteImgFile(String name) {
        QueryWrapper<NodeInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nodeUserPasswd", vmMapper.selectById(name).getServerip());
        NodeInfo nodeInfo = nodeMapper.selectOne(queryWrapper);
        ChannelSftp channel=SftpUtils.getSftpcon(nodeInfo.getNodeUserPasswd());
        channel.cd("/home/qemuVM/VM_place/");
        channel.rm(name);
        SftpUtils.discon();
    }

    /**
     * 关闭网络
     */
    @SneakyThrows
    public void closeNetWork() {
        Domain domain = getDomainByName(getVirtualList().get(0).getName());
        Network network = domain.getConnect().networkLookupByName("default");
        if (network.isActive() == 1) {
            network.destroy();
            log.info("网络" + network.getName() + "已经被关闭！");
        } else log.info("网络" + network.getName() + "已经处于关闭状态！");
    }

    /**
     * 启动网络
     */
    @SneakyThrows
    public void openNetWork() {
        Domain domain = getDomainByName(getVirtualList().get(0).getName());
        Network network = domain.getConnect().networkLookupByName("default");
        if (network.isActive() == 0) {
            network.create();
            log.info("网络" + network.getName() + "已经被打开！");
        } else log.info("网络" + network.getName() + "已经处于打开状态！");
    }

    /**
     * 网络 State
     */
    @SneakyThrows
    public String getNetState() {
        if(getVirtualList().isEmpty()) return "off";
        Domain domain = getDomainByName(getVirtualList().get(0).getName());
        if (domain.getConnect().networkLookupByName("default").isActive() == 1) return "on";
        else return "off";
    }

    /**
     * getSnapshotList
     */
    @SneakyThrows
    public List<Snapshot> getSnapshotListByName(String name) {
        // virsh snapshot-list      虚拟机名
        String cmd = "virsh snapshot" + "-list " + name;
        Process process = Runtime.getRuntime().exec(cmd);
        LineNumberReader line = new LineNumberReader(new InputStreamReader(process.getInputStream()));
        ArrayList<Snapshot> snapshots = new ArrayList<>();
        String str;
        int linCount = 0;
        int snapshotNum = getDomainByName(name).snapshotNum(); // 2
        while ((str = line.readLine()) != null && snapshotNum > 0) {
            linCount++;
            if (linCount <= 2) continue;  // -2 line
            else {
                snapshotNum--;
                String[] lineStr = str.split("   ");
                snapshots.add(Snapshot.builder().name(lineStr[0]).creationTime(lineStr[1]).state(lineStr[2]).build());
            }
        }
        return snapshots;
    }

    /**
     * snapshot管理
     */
    @SneakyThrows
    private void snapshotManger(String op, String name, String snapshotName) {
        // virsh snapshot-create-as 虚拟机名称 快照名称
        // virsh snapshot-delete    虚拟机名称 快照名称
        // virsh snapshot-revert    虚拟机名称 快照名称
        String cmd = "";
        switch (op) {
            case "creat":
                cmd = "virsh snapshot" + "-create-as " + name + " " + snapshotName;
                break;
            case "delete":
                cmd = "virsh snapshot" + "-delete " + name + " " + snapshotName;
                break;
            case "revert":
                cmd = "virsh snapshot" + "-revert " + name + " " + snapshotName;
                break;
        }
        Runtime.getRuntime().exec(cmd);
    }

    /**
     * 创建快照
     */
    public void createSnapshot(String name, String snapshotName) {
        snapshotManger("creat", name, snapshotName);
        log.info("虚拟机" + name + "成功创建快照" + snapshotName);
    }

    /**
     * 删除快照
     */
    public void deleteSnapshot(String name, String snapshotName) {
        snapshotManger("delete", name, snapshotName);
        log.info("虚拟机" + name + "成功删除快照" + snapshotName);
    }

    /**
     * 启动快照
     */
    public void revertSnapshot(String name, String snapshotName) {
        snapshotManger("revert", name, snapshotName);
        log.info("虚拟机" + name + "成功切换快照" + snapshotName);
    }

    /**
     * getStoragePoolList
     */
    @SneakyThrows
    public List<Storagepool> getStoragePoolList() {
        String[] pools = LibvirtUtils.getConnection().listStoragePools();
        String[] definedPools = LibvirtUtils.getConnection().listDefinedStoragePools();
        log.info("pools" + Arrays.toString(pools) + "definedPools" + Arrays.toString(definedPools));
        List<Storagepool> storagePoolList = new ArrayList<>();
        for (String pool : pools) storagePoolList.add(getStoragePool(pool));
        for (String pool : definedPools) storagePoolList.add(getStoragePool(pool));
        return storagePoolList;
    }

    /**
     * getStoragePool ByName
     */
    @SneakyThrows
    public Storagepool getStoragePool(String name) {
        StoragePool storagePool = LibvirtUtils.getConnection().storagePoolLookupByName(name);
        StoragePoolInfo info = storagePool.getInfo();
        return Storagepool.builder()
                .name(name)     // 名称
                .type("qcow2")  // 类型
                .capacity((int) (info.capacity / 1024.00 / 1024.00 / 1024.00))     // GB 容量
                .available((int) (info.available / 1024.00 / 1024.00 / 1024.00))   // GB 可用容量
                .allocation((int) (info.allocation / 1024.00 / 1024.00 / 1024.00)) // GB 已用容量
                .usage(((int) ((info.allocation / 1024.00 / 1024.00 / 1024.00) / (info.capacity / 1024.00 / 1024.00 / 1024.00) * 100)) + "%") // 使用率(%)
                .state(info.state.toString())                // 状态
                .xml(storagePool.getXMLDesc(0))        // 描述xml
                .build();
    }

    /**
     * 删除StoragePool ByName
     */
    @SneakyThrows
    public void deleteStoragePool(String name) {
        StoragePool storagePool = LibvirtUtils.getConnection().storagePoolLookupByName(name);
        for (String pool : LibvirtUtils.getConnection().listStoragePools())
            if (pool.equals(name)) storagePool.destroy();
        for (String pool : LibvirtUtils.getConnection().listDefinedStoragePools())
            if (pool.equals(name)) storagePool.undefine();
    }

    /**
     * 创建Storagepool  >>>>>> url必须存在
     */
    @SneakyThrows
    public boolean createStoragepool(String name, String url) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<pool type=\"dir\">\n" +
                "    <name>" + name + "</name> \n" +       // 名称必须唯一
                "    <source>\n" +
                "    </source>\n" +
                "    <target>\n" +
                "        <path>" + url + "</path> \n" +                     // StoragePool 在宿主机的路径
                "        <permissions> \n" +                                // 权限
                "            <mode>0711</mode>\n" +
                "            <owner>0</owner>\n" +
                "            <group>0</group>\n" +
                "        </permissions>\n" +
                "    </target>\n" +
                "</pool>";
        return LibvirtUtils.getConnection().storagePoolCreateXML(xml, 0) == null ? false : true;
    }


    @SneakyThrows
    public static void main(String[] args) {
        LibvirtService libvirtService = new LibvirtService();


//        for (Virtual virtual : libvirtService.getVirtualList()) System.out.println(virtual);

//        libvirtService.createSnapshot("ubuntu14.04", "sna2");
//        libvirtService.deleteSnapshot("ubuntu14.04", "sna3");
//        libvirtService.revertSnapshot("ubuntu14.04", "sna2");
//        System.out.println(libvirtService.getSnapshotListByName("ubuntu14.04"));


//        System.out.println(libvirtService.createStoragepool("bsy", "/home/bsy"));
//        for (Storagepool storagePool : libvirtService.getStoragePoolList()) System.out.println(storagePool);
//        libvirtService.deleteStoragePool("bsy");
//        for (Storagepool storagePool : libvirtService.getStoragePoolList()) System.out.println(storagePool);

//        System.out.println(libvirtService.getNetState());

    }



}
