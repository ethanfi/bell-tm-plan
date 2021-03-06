package cn.edu.bnuz.bell.planning

import cn.edu.bnuz.bell.http.NotFoundException
import cn.edu.bnuz.bell.master.TermService
import cn.edu.bnuz.bell.utils.CollectionUtils
import cn.edu.bnuz.bell.utils.GroupCondition
import cn.edu.bnuz.bell.workflow.State
import grails.gorm.transactions.Transactional

/**
 * 培养方案公共视图服务
 * @author Yang Lin
 */
@Transactional(readOnly = true)
class VisionPublicService {
    SchemePublicService schemePublicService
    TermService termService

    /**
     * 获取已审核培养方案（除专升本和课程班）
     * @return 培养方案列表
     */
    def getAllVisions() {
        def startGrade = termService.minInSchoolGrade
        List results = Vision.executeQuery '''
select new map(
  v.id as id,
  program.id as programId,
  department.name as departmentName,
  department.id as departmentId,
  subject.id as subjectId,
  program.type as programType,
  subject.name as subjectName,
  major.grade as grade
)
from Vision v
join v.program program
join program.major major
join major.subject subject
join subject.department department
where subject.isTopUp = false
and major.degree is not null
and major.grade >= :startGrade
and v.versionNumber = (
  select max(v2.versionNumber)
  from Vision v2
  where v2.status = :status
  and v2.program = v.program
)
order by department.id, subject.id, major.grade
''', [startGrade: startGrade, status: State.APPROVED]

        List<GroupCondition> conditions = [
                new GroupCondition(
                        groupBy: 'departmentId',
                        into: 'subjects',
                        mappings: [
                                departmentId  : 'id',
                                departmentName: 'name'
                        ]
                ),
                new GroupCondition(
                        groupBy: 'subjectId',
                        into: 'grades',
                        mappings: [
                                subjectId  : 'id',
                                subjectName: 'name',
                                programType: 'type'
                        ]
                )
        ]

        CollectionUtils.groupBy(results, conditions) { items ->
            items.inject([:]) { acc, item -> acc[item.grade] = item.id; acc }
        }
    }

    /**
     * 获取指定的培养方案信息（用于显示）。
     * @param id 培养方案ID
     * @return 培养方案信息
     */
    Map getVisionInfo(Long id) throws NotFoundException {
        List<Map> results = Vision.executeQuery '''
select new map(
  vision.id as id,
  vision.versionNumber as versionNumber,
  prev.id as previousId,
  prev.versionNumber as previousVersionNumber,
  program.id as programId,
  program.type as programType,
  subject.name as subjectName,
  department.id as departmentId,
  department.name as departmentName,
  major.grade as grade,
  vision.objective as objective,
  vision.specification as specification,
  vision.schoolingLength as schoolingLength,
  vision.awardedDegree as awardedDegree,
  degree.name as degreeName,
  vision.status as status,
  vision.workflowInstance.id as workflowInstanceId
)
from Vision vision
join vision.program program
join program.major major
join major.subject subject
join major.degree degree
join major.department department
left join vision.previous prev
where vision.id = :id
''', [id: id]
        if(!results) {
            throw new NotFoundException()
        }

        def vision = results[0]
        Integer programId = vision.programId
        vision.schemeId = schemePublicService.getLatestSchemeId(programId)

        vision
    }
}
