<template>
    <div  class="app-container">
        <el-table
            :data="list"
            style="width: 100%"
            row-key="id"
            border
            lazy
            :load="load"
            :tree-props="{children: 'children', hasChildren: 'hasChildren'}">

            <el-table-column
            prop="name"
            label="名称"
            width="150">
            </el-table-column>

            <el-table-column
            prop="dictCode"
            label="编码"
            width="150">
            </el-table-column>

            <el-table-column
            prop="value"
            label="值"
            width="150">
            </el-table-column>

            <el-table-column
            prop="createTime"
            label="创建时间">
            </el-table-column>
        </el-table>
        <div class="el-toolbar-body" style="justify-content: flex-start;">
            <el-button type="text" @click="exportData"><i class="fa fa-plus"/> 导出</el-button>
            <el-button type="text" @click="importData"><i class="fa fa-plus"/> 导入</el-button>
        </div>

        <el-dialog title="导入" :visible.sync="dialogImportVisible" width="480px">
            <el-form label-position="right" label-width="170px">
                <el-form-item label="文件">
                    <el-upload
                            :multiple="false"
                            :on-success="onUploadSuccess"
                            :action="'http://localhost:9001/admin/cmn/dict/importData'"
                            class="upload-demo">
                        <el-button size="small" type="primary">点击上传</el-button>
                        <div slot="tip" class="el-upload__tip">只能上传xls文件,且不超过500kb</div>
                    </el-upload>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogImportVisible = false">取消</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import dict from '@/api/yygh/dict'
export default {
    data() {
        return {
            list:[], //数据字典列表数组
            dialogImportVisible:false //默认是false,不显示弹窗
        }
    },
    created() {
        this.getDictList(1) //在数据渲染页面之前获取顶级节点(pid=1):省、医院等级、证件类型、学历、民族
    },
    methods: {
        //数据字典列表
        getDictList(id) {
            dict.dictList(id).then(response => {
                this.list = response.data.list
            })
        },
        //该方法用于点击展开'>'时，显示数据
        load(tree, treeNode, resolve) {
            //tree表示当前行数据,resolve是一个函数,作用是将当前元素的子元素挂载到当前元素下
            //tree.id作为父id,再次查询子元素,然后resolve进行挂载
            dict.dictList(tree.id).then(response => {
                resolve(response.data.list)
            })
        },
        //导出数据字典
        exportData() {
            //直接请求后端接口
            window.open("http://localhost:9001/admin/cmn/dict/exportData")
        },
        //文件导入
        importData() {
            this.dialogImportVisible = true //显示弹窗
        },
        //文件导入成功后触发的方法
        onUploadSuccess(response, file) {
            this.$message.info('上传成功')
            this.dialogImportVisible = false //关闭弹窗
            this.getDictList(1) //再次请求后端刷新数据
        }
    }
}
</script>